package com.financecoach.ai;

import com.financecoach.entity.*;
import com.financecoach.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Component
public class FinancialContextBuilder {

    private final FinancialProfileRepository profileRepo;
    private final TransactionRepository txRepo;
    private final FinancialGoalRepository goalRepo;
    private final BudgetRepository budgetRepo;
    private final SubscriptionRepository subscriptionRepo;

    public FinancialContextBuilder(
            FinancialProfileRepository profileRepo,
            TransactionRepository txRepo,
            FinancialGoalRepository goalRepo,
            BudgetRepository budgetRepo,
            SubscriptionRepository subscriptionRepo) {
        this.profileRepo = profileRepo;
        this.txRepo = txRepo;
        this.goalRepo = goalRepo;
        this.budgetRepo = budgetRepo;
        this.subscriptionRepo = subscriptionRepo;
    }

    @Transactional(readOnly = true)
    public FinancialContextSummary buildContext(Long userId) {
        FinancialContextSummary summary = new FinancialContextSummary();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

        // 1. Profile information
        FinancialProfile profile = profileRepo.findByUserId(userId).orElse(null);
        BigDecimal profileIncome = profile != null && profile.getMonthlyIncome() != null
                ? profile.getMonthlyIncome() : BigDecimal.ZERO;
        BigDecimal profileExpenses = profile != null && profile.getMonthlyFixedExpenses() != null
                ? profile.getMonthlyFixedExpenses() : BigDecimal.ZERO;
        BigDecimal savingsTarget = profile != null && profile.getSavingsTarget() != null
                ? profile.getSavingsTarget() : BigDecimal.valueOf(20.00);

        summary.setTargetSavingsRate(savingsTarget.setScale(2, RoundingMode.HALF_UP));
        if (profile != null && profile.getEmergencyFund() != null) {
            summary.setEmergencyFundGoal(profile.getEmergencyFund().setScale(2, RoundingMode.HALF_UP));
        }

        // 2. Actual monthly transactions
        BigDecimal actualIncome = txRepo.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.INCOME, startOfMonth, endOfMonth);
        BigDecimal actualExpenses = txRepo.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startOfMonth, endOfMonth);

        BigDecimal income = (actualIncome != null && actualIncome.compareTo(BigDecimal.ZERO) > 0)
                ? actualIncome : profileIncome;
        BigDecimal expenses = (actualExpenses != null && actualExpenses.compareTo(BigDecimal.ZERO) > 0)
                ? actualExpenses : profileExpenses;

        BigDecimal netSavings = income.subtract(expenses);
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(income, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        summary.setMonthlyIncome(income.setScale(2, RoundingMode.HALF_UP));
        summary.setMonthlyExpenses(expenses.setScale(2, RoundingMode.HALF_UP));
        summary.setNetSavings(netSavings.setScale(2, RoundingMode.HALF_UP));
        summary.setSavingsRate(savingsRate);

        // 3. Top Expense Categories
        List<Object[]> rawCategoryData = txRepo.findCategorySpendingSummary(
                userId, TransactionType.EXPENSE, startOfMonth, endOfMonth);
        List<FinancialContextSummary.CategoryDetail> categoryDetails = new ArrayList<>();

        if (rawCategoryData != null) {
            for (Object[] row : rawCategoryData) {
                if (categoryDetails.size() >= 5) break; // Top 5
                String cat = (String) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                double pct = 0.0;
                if (expenses.compareTo(BigDecimal.ZERO) > 0 && amount != null) {
                    pct = amount.divide(expenses, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                }
                categoryDetails.add(new FinancialContextSummary.CategoryDetail(
                        cat != null ? cat : "Other",
                        amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                        Math.round(pct * 10.0) / 10.0
                ));
            }
        }
        summary.setTopCategories(categoryDetails);

        // 4. Financial Goals
        List<FinancialGoal> goals = goalRepo.findByUserId(userId, Sort.by("targetDate"));
        List<FinancialContextSummary.GoalDetail> goalDetails = new ArrayList<>();
        for (FinancialGoal g : goals) {
            if (g.getStatus() == GoalStatus.COMPLETED) continue;

            BigDecimal target = g.getTargetAmount() != null ? g.getTargetAmount() : BigDecimal.ZERO;
            BigDecimal current = g.getCurrentAmount() != null ? g.getCurrentAmount() : BigDecimal.ZERO;
            BigDecimal remaining = target.subtract(current).max(BigDecimal.ZERO);

            long monthsRemaining = 1;
            if (g.getTargetDate() != null) {
                monthsRemaining = Math.max(1, ChronoUnit.MONTHS.between(now, g.getTargetDate()));
            }

            BigDecimal reqMonthly = remaining.divide(BigDecimal.valueOf(monthsRemaining), 2, RoundingMode.CEILING);

            goalDetails.add(new FinancialContextSummary.GoalDetail(
                    g.getName(),
                    target.setScale(2, RoundingMode.HALF_UP),
                    current.setScale(2, RoundingMode.HALF_UP),
                    remaining.setScale(2, RoundingMode.HALF_UP),
                    g.getTargetDate() != null ? g.getTargetDate().toString() : "No date",
                    monthsRemaining,
                    reqMonthly
            ));
        }
        summary.setActiveGoals(goalDetails);

        // 5. Budgets
        List<Budget> budgets = budgetRepo.findByUserId(userId);
        List<FinancialContextSummary.BudgetDetail> budgetDetails = new ArrayList<>();
        for (Budget b : budgets) {
            BigDecimal limit = b.getMonthlyLimit() != null ? b.getMonthlyLimit() : BigDecimal.ZERO;
            BigDecimal spent = txRepo.sumAmountByUserIdAndTypeAndCategoryAndDateBetween(
                    userId, TransactionType.EXPENSE, b.getCategory(), startOfMonth, endOfMonth);
            if (spent == null) spent = BigDecimal.ZERO;

            double utilPct = 0.0;
            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                utilPct = spent.divide(limit, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            }
            boolean exceeded = spent.compareTo(limit) > 0;

            budgetDetails.add(new FinancialContextSummary.BudgetDetail(
                    b.getCategory(),
                    limit.setScale(2, RoundingMode.HALF_UP),
                    spent.setScale(2, RoundingMode.HALF_UP),
                    Math.round(utilPct * 10.0) / 10.0,
                    exceeded
            ));
        }
        summary.setActiveBudgets(budgetDetails);

        // 6. Subscriptions
        List<Subscription> subscriptions = subscriptionRepo.findByUserIdAndActive(userId, true, Sort.by("nextPaymentDate"));
        List<FinancialContextSummary.SubscriptionDetail> subDetails = new ArrayList<>();
        BigDecimal totalSubMonthly = BigDecimal.ZERO;

        for (Subscription s : subscriptions) {
            BigDecimal amt = s.getAmount() != null ? s.getAmount() : BigDecimal.ZERO;
            BigDecimal monthlyEq = amt;
            if (s.getBillingFrequency() == BillingFrequency.YEARLY) {
                monthlyEq = amt.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            } else if (s.getBillingFrequency() == BillingFrequency.QUARTERLY) {
                monthlyEq = amt.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            } else if (s.getBillingFrequency() == BillingFrequency.WEEKLY) {
                monthlyEq = amt.multiply(BigDecimal.valueOf(4.33)).setScale(2, RoundingMode.HALF_UP);
            }

            totalSubMonthly = totalSubMonthly.add(monthlyEq);
            subDetails.add(new FinancialContextSummary.SubscriptionDetail(
                    s.getName(),
                    amt.setScale(2, RoundingMode.HALF_UP),
                    s.getBillingFrequency() != null ? s.getBillingFrequency().name() : "MONTHLY",
                    monthlyEq.setScale(2, RoundingMode.HALF_UP)
            ));
        }
        summary.setActiveSubscriptions(subDetails);
        summary.setTotalMonthlySubscriptions(totalSubMonthly.setScale(2, RoundingMode.HALF_UP));

        // Format prompt text
        summary.setPromptContextText(buildPromptText(summary));
        return summary;
    }

    private String buildPromptText(FinancialContextSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== USER FINANCIAL CONTEXT ===\n");
        sb.append(String.format("- Monthly Income: ₹%s\n", summary.getMonthlyIncome()));
        sb.append(String.format("- Monthly Expenses: ₹%s\n", summary.getMonthlyExpenses()));
        sb.append(String.format("- Monthly Net Savings: ₹%s\n", summary.getNetSavings()));
        sb.append(String.format("- Current Savings Rate: %s%% (Target: %s%%)\n",
                summary.getSavingsRate(), summary.getTargetSavingsRate()));

        if (!summary.getTopCategories().isEmpty()) {
            sb.append("\nTOP EXPENSE CATEGORIES:\n");
            for (FinancialContextSummary.CategoryDetail cd : summary.getTopCategories()) {
                sb.append(String.format("- %s: ₹%s (%.1f%% of expenses)\n",
                        cd.category(), cd.amount(), cd.percentOfExpenses()));
            }
        }

        if (!summary.getActiveGoals().isEmpty()) {
            sb.append("\nACTIVE FINANCIAL GOALS:\n");
            for (FinancialContextSummary.GoalDetail gd : summary.getActiveGoals()) {
                sb.append(String.format("- %s: Saved ₹%s of ₹%s (Remaining: ₹%s | Target: %s, %d mo left | Req: ₹%s/mo)\n",
                        gd.name(), gd.currentAmount(), gd.targetAmount(),
                        gd.remainingAmount(), gd.targetDate(), gd.monthsRemaining(), gd.requiredMonthlySavings()));
            }
        }

        if (!summary.getActiveBudgets().isEmpty()) {
            sb.append("\nACTIVE BUDGETS:\n");
            for (FinancialContextSummary.BudgetDetail bd : summary.getActiveBudgets()) {
                sb.append(String.format("- %s: Spent ₹%s of ₹%s limit (%.1f%%%s)\n",
                        bd.category(), bd.currentSpent(), bd.monthlyLimit(),
                        bd.utilizationPercent(), bd.exceeded() ? " - OVERRUN" : ""));
            }
        }

        if (!summary.getActiveSubscriptions().isEmpty()) {
            sb.append(String.format("\nRECURRING SUBSCRIPTIONS (Total: ₹%s/mo):\n", summary.getTotalMonthlySubscriptions()));
            for (FinancialContextSummary.SubscriptionDetail sd : summary.getActiveSubscriptions()) {
                sb.append(String.format("- %s: ₹%s (%s, ~₹%.2f/mo)\n",
                        sd.name(), sd.amount(), sd.frequency(), sd.monthlyEquivalent().doubleValue()));
            }
        }

        return sb.toString();
    }
}
