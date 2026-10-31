package com.financecoach.service;

import com.financecoach.dto.CategorySpendingDto;
import com.financecoach.dto.DashboardSummaryDto;
import com.financecoach.dto.FinancialProfileDto;
import com.financecoach.entity.GoalStatus;
import com.financecoach.entity.TransactionType;
import com.financecoach.repository.FinancialGoalRepository;
import com.financecoach.repository.SubscriptionRepository;
import com.financecoach.repository.TransactionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final FinancialProfileService profileService;
    private final FinancialGoalRepository goalRepository;
    private final SubscriptionRepository subscriptionRepository;

    public DashboardService(TransactionRepository transactionRepository,
                            FinancialProfileService profileService,
                            FinancialGoalRepository goalRepository,
                            SubscriptionRepository subscriptionRepository) {
        this.transactionRepository = transactionRepository;
        this.profileService = profileService;
        this.goalRepository = goalRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Long userId, Integer month, Integer year) {
        LocalDate startDate;
        LocalDate endDate;

        if (month != null && year != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            LocalDate now = LocalDate.now();
            startDate = now.with(TemporalAdjusters.firstDayOfMonth());
            endDate = now.with(TemporalAdjusters.lastDayOfMonth());
        }

        // 1. Calculate Monthly Income & Expenses from transactions
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.INCOME, startDate, endDate
        );
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.EXPENSE, startDate, endDate
        );
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }

        // 2. Net Savings = Total Income - Total Expenses
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        // 3. Savings Rate = Net Savings / Total Income * 100 (Safe check for zero income)
        BigDecimal savingsRate = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }

        // 4. Fetch Living Financial Profile
        FinancialProfileDto profile = profileService.getProfileByUserId(userId);

        // 5. Goals and Subscriptions counts
        long goalCount = goalRepository.findByUserIdAndStatus(userId, GoalStatus.IN_PROGRESS, Sort.unsorted()).size();
        long activeSubCount = subscriptionRepository.findByUserIdAndActive(userId, true, Sort.unsorted()).size();

        // 6. Category Spending Breakdown
        List<CategorySpendingDto> topCategories = getCategorySpending(userId, startDate, endDate, totalExpenses);

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setTotalIncome(totalIncome.setScale(2, RoundingMode.HALF_UP));
        summary.setTotalExpenses(totalExpenses.setScale(2, RoundingMode.HALF_UP));
        summary.setNetSavings(netSavings.setScale(2, RoundingMode.HALF_UP));
        summary.setSavingsRate(savingsRate);
        summary.setCurrentSavings(profile.getCurrentSavings());
        summary.setEmergencyFund(profile.getEmergencyFund());
        summary.setMonthlyFixedExpenses(profile.getMonthlyFixedExpenses());
        summary.setSavingsTarget(profile.getSavingsTarget());
        summary.setGoalCount(goalCount);
        summary.setActiveSubscriptionCount(activeSubCount);
        summary.setTopSpendingCategories(topCategories);

        return summary;
    }

    @Transactional(readOnly = true)
    public List<CategorySpendingDto> getCategorySpending(Long userId, LocalDate startDate, LocalDate endDate, BigDecimal totalExpenses) {
        List<Object[]> rows = transactionRepository.findCategorySpendingSummary(
            userId, TransactionType.EXPENSE, startDate, endDate
        );

        List<CategorySpendingDto> list = new ArrayList<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            long count = ((Number) row[2]).longValue();

            double percentage = 0.0;
            if (totalExpenses != null && totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            }

            list.add(new CategorySpendingDto(category, amount.setScale(2, RoundingMode.HALF_UP), percentage, count));
        }

        return list;
    }

    @Transactional(readOnly = true)
    public List<CategorySpendingDto> getCategorySpendingForMonth(Long userId, Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        LocalDate startDate = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.EXPENSE, startDate, endDate
        );
        if (totalExpenses == null) {
            totalExpenses = BigDecimal.ZERO;
        }

        return getCategorySpending(userId, startDate, endDate, totalExpenses);
    }
}

