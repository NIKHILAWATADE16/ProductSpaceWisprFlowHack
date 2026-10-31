package com.financecoach.service;

import com.financecoach.dto.*;
import com.financecoach.entity.*;
import com.financecoach.repository.FinancialGoalRepository;
import com.financecoach.repository.FinancialProfileRepository;
import com.financecoach.repository.TransactionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoachService {

    private final TransactionRepository tx;
    private final FinancialGoalRepository goalRepo;
    private final FinancialProfileRepository profileRepo;

    public CoachService(TransactionRepository tx,
                        FinancialGoalRepository goalRepo,
                        FinancialProfileRepository profileRepo) {
        this.tx = tx;
        this.goalRepo = goalRepo;
        this.profileRepo = profileRepo;
    }

    @Transactional(readOnly = true)
    public SimulationResponse simulate(Long uid, SimulationRequest req) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal income = tx.sumAmountByUserIdAndTypeAndDateBetween(uid, TransactionType.INCOME, start, end);
        BigDecimal expense = tx.sumAmountByUserIdAndTypeAndDateBetween(uid, TransactionType.EXPENSE, start, end);

        // Fallback to Living Financial Profile if user has zero recorded transactions this month
        if (income == null || income.compareTo(BigDecimal.ZERO) == 0) {
            income = profileRepo.findByUserId(uid).map(FinancialProfile::getMonthlyIncome).orElse(BigDecimal.ZERO);
        }
        if (expense == null || expense.compareTo(BigDecimal.ZERO) == 0) {
            expense = profileRepo.findByUserId(uid).map(FinancialProfile::getMonthlyFixedExpenses).orElse(BigDecimal.ZERO);
        }

        BigDecimal incomeChange = req.getIncomeChange() != null ? req.getIncomeChange() : BigDecimal.ZERO;
        BigDecimal expenseChange = req.getExpenseChange() != null ? req.getExpenseChange() : BigDecimal.ZERO;

        BigDecimal newIncome = income.add(incomeChange).max(BigDecimal.ZERO);
        BigDecimal newExpense = expense.add(expenseChange).max(BigDecimal.ZERO);

        BigDecimal baselineNet = income.subtract(expense);
        BigDecimal newNet = newIncome.subtract(newExpense);
        BigDecimal delta = newNet.subtract(baselineNet);

        BigDecimal newSavingsRate = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (newIncome.compareTo(BigDecimal.ZERO) > 0) {
            newSavingsRate = newNet.divide(newIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        // Project impact on each in-progress financial goal
        List<FinancialGoal> goals = goalRepo.findByUserId(uid, Sort.by("targetDate"));
        List<GoalProjectionDto> projections = new ArrayList<>();

        for (FinancialGoal g : goals) {
            if (g.getStatus() == GoalStatus.COMPLETED) continue;

            BigDecimal target = g.getTargetAmount() != null ? g.getTargetAmount() : BigDecimal.ZERO;
            BigDecimal current = g.getCurrentAmount() != null ? g.getCurrentAmount() : BigDecimal.ZERO;
            BigDecimal remaining = target.subtract(current).max(BigDecimal.ZERO);

            long baselineMonths = Math.max(1, ChronoUnit.MONTHS.between(now, g.getTargetDate()));

            // Determine baseline monthly savings for this goal
            BigDecimal baselineMonthlySavings = remaining.divide(BigDecimal.valueOf(baselineMonths), 2, RoundingMode.CEILING);
            // Simulated monthly allocation incorporates a portion of the delta
            BigDecimal newMonthlySavings = baselineMonthlySavings.add(delta.multiply(BigDecimal.valueOf(0.5))).max(BigDecimal.valueOf(1));

            long projectedMonths = remaining.divide(newMonthlySavings, 0, RoundingMode.CEILING).longValue();
            if (projectedMonths <= 0) projectedMonths = 1;

            LocalDate projectedDate = now.plusMonths(projectedMonths);
            long monthsSaved = baselineMonths - projectedMonths;

            String summary;
            if (monthsSaved > 0) {
                summary = "Completes " + monthsSaved + " month" + (monthsSaved > 1 ? "s" : "") + " earlier (" + projectedDate + ")";
            } else if (monthsSaved < 0) {
                summary = "Delayed by " + Math.abs(monthsSaved) + " month" + (Math.abs(monthsSaved) > 1 ? "s" : "") + " (" + projectedDate + ")";
            } else {
                summary = "On track for " + g.getTargetDate();
            }

            projections.add(new GoalProjectionDto(
                g.getId(),
                g.getName(),
                target.setScale(2, RoundingMode.HALF_UP),
                current.setScale(2, RoundingMode.HALF_UP),
                remaining.setScale(2, RoundingMode.HALF_UP),
                g.getTargetDate(),
                projectedDate,
                baselineMonths,
                projectedMonths,
                monthsSaved,
                summary
            ));
        }

        String summaryText;
        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            summaryText = "Simulated changes increase your monthly net savings by " + delta.setScale(2, RoundingMode.HALF_UP) +
                ", boosting your savings rate to " + newSavingsRate + "%.";
        } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
            summaryText = "Simulated changes reduce your monthly net savings by " + delta.abs().setScale(2, RoundingMode.HALF_UP) +
                ", lowering your savings rate to " + newSavingsRate + "%.";
        } else {
            summaryText = "Simulated changes keep your monthly savings rate unchanged at " + newSavingsRate + "%.";
        }

        SimulationResponse out = new SimulationResponse();
        out.setBaselineIncome(income.setScale(2, RoundingMode.HALF_UP));
        out.setBaselineExpenses(expense.setScale(2, RoundingMode.HALF_UP));
        out.setBaselineNetSavings(baselineNet.setScale(2, RoundingMode.HALF_UP));
        out.setNewIncome(newIncome.setScale(2, RoundingMode.HALF_UP));
        out.setNewExpenses(newExpense.setScale(2, RoundingMode.HALF_UP));
        out.setNewNetSavings(newNet.setScale(2, RoundingMode.HALF_UP));
        out.setMonthlySavingsDelta(delta.setScale(2, RoundingMode.HALF_UP));
        out.setNewSavingsRate(newSavingsRate);
        out.setSummaryText(summaryText);
        out.setGoalProjections(projections);

        return out;
    }

    @Transactional(readOnly = true)
    public CoachResponse chat(Long uid, String question) {
        String q = question == null ? "" : question.toLowerCase();
        if (q.contains("spend") || q.contains("highest")) {
            return new CoachResponse("Review your monthly category report to identify the largest expense areas. I can only provide guidance from recorded transactions.");
        }
        if (q.contains("save") || q.contains("savings")) {
            return new CoachResponse("Use the simulator to test income or expense changes, then compare the resulting savings rate before changing your plan.");
        }
        return new CoachResponse("I can help explain recorded spending, savings rates, goals, and what-if scenarios. Try asking where spending is highest or how to improve savings.");
    }
}
