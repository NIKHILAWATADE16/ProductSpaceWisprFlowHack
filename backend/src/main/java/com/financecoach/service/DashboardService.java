package com.financecoach.service;

import com.financecoach.dto.CategorySpendingDto;
import com.financecoach.dto.DashboardSummaryDto;
import com.financecoach.dto.FinancialProfileDto;
import com.financecoach.dto.SpendingNudgeDto;
import com.financecoach.dto.SubscriptionDto;
import com.financecoach.entity.Budget;
import com.financecoach.entity.GoalStatus;
import com.financecoach.entity.TransactionType;
import com.financecoach.repository.BudgetRepository;
import com.financecoach.repository.FinancialGoalRepository;
import com.financecoach.repository.SubscriptionRepository;
import com.financecoach.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final BudgetRepository budgetRepository;
    private final SubscriptionService subscriptionService;

    @Autowired
    public DashboardService(TransactionRepository transactionRepository,
                            FinancialProfileService profileService,
                            FinancialGoalRepository goalRepository,
                            SubscriptionRepository subscriptionRepository,
                            BudgetRepository budgetRepository,
                            SubscriptionService subscriptionService) {
        this.transactionRepository = transactionRepository;
        this.profileService = profileService;
        this.goalRepository = goalRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.budgetRepository = budgetRepository;
        this.subscriptionService = subscriptionService;
    }

    public DashboardService(TransactionRepository transactionRepository,
                            FinancialProfileService profileService,
                            FinancialGoalRepository goalRepository,
                            SubscriptionRepository subscriptionRepository) {
        this(transactionRepository, profileService, goalRepository, subscriptionRepository, null, null);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Long userId, Integer month, Integer year) {
        LocalDate startDate;
        LocalDate endDate;

        if (month != null && year != null) {
            validatePeriod(month, year);
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

        // 7. Spending Drift Nudges
        List<SpendingNudgeDto> nudges = computeNudges(userId, startDate.getMonthValue(), startDate.getYear(),
            startDate, endDate, totalIncome, totalExpenses, netSavings, savingsRate, profile);

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
        summary.setNudges(nudges);

        return summary;
    }

    @Transactional(readOnly = true)
    public List<SpendingNudgeDto> getSpendingNudges(Long userId, Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();
        validatePeriod(targetMonth, targetYear);

        LocalDate startDate = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.INCOME, startDate, endDate
        );
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.EXPENSE, startDate, endDate
        );
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        BigDecimal savingsRate = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }

        FinancialProfileDto profile = profileService.getProfileByUserId(userId);
        return computeNudges(userId, targetMonth, targetYear, startDate, endDate,
            totalIncome, totalExpenses, netSavings, savingsRate, profile);
    }

    private List<SpendingNudgeDto> computeNudges(Long userId, int month, int year, LocalDate startDate, LocalDate endDate,
                                                BigDecimal totalIncome, BigDecimal totalExpenses,
                                                BigDecimal netSavings, BigDecimal savingsRate,
                                                FinancialProfileDto profile) {
        List<SpendingNudgeDto> nudges = new ArrayList<>();

        // 1. Budget Drifts (Overrun & Warning)
        if (budgetRepository != null) {
            List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
            for (Budget b : budgets) {
                BigDecimal spent = transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndDateBetween(
                    userId, TransactionType.EXPENSE, b.getCategory(), startDate, endDate
                );
                if (spent == null) spent = BigDecimal.ZERO;

                BigDecimal limit = b.getMonthlyLimit();
                if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) continue;

                if (spent.compareTo(limit) > 0) {
                    BigDecimal overrun = spent.subtract(limit).setScale(2, RoundingMode.HALF_UP);
                    nudges.add(new SpendingNudgeDto(
                        "BUDGET_OVERRUN",
                        "HIGH",
                        b.getCategory() + " Budget Exceeded!",
                        String.format("You have spent $%s of your $%s %s budget ($%s over limit).",
                            spent.setScale(2, RoundingMode.HALF_UP), limit.setScale(2, RoundingMode.HALF_UP),
                            b.getCategory(), overrun),
                        b.getCategory(),
                        limit,
                        spent,
                        "/budgets"
                    ));
                } else {
                    BigDecimal ratio = spent.divide(limit, 4, RoundingMode.HALF_UP);
                    if (ratio.compareTo(BigDecimal.valueOf(0.80)) >= 0) {
                        BigDecimal pct = ratio.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
                        nudges.add(new SpendingNudgeDto(
                            "BUDGET_WARNING",
                            "MEDIUM",
                            b.getCategory() + " Approaching Limit",
                            String.format("You have reached %s%% of your $%s %s budget ($%s spent).",
                                pct.toPlainString(), limit.setScale(2, RoundingMode.HALF_UP),
                                b.getCategory(), spent.setScale(2, RoundingMode.HALF_UP)),
                            b.getCategory(),
                            limit,
                            spent,
                            "/budgets"
                        ));
                    }
                }
            }
        }

        // 2. Savings Rate Drift
        if (profile != null && profile.getSavingsTarget() != null && profile.getSavingsTarget().compareTo(BigDecimal.ZERO) > 0) {
            if (netSavings.compareTo(BigDecimal.ZERO) < 0) {
                nudges.add(new SpendingNudgeDto(
                    "SAVINGS_RATE_DRIFT",
                    "HIGH",
                    "Negative Monthly Cashflow",
                    String.format("Expenses ($%s) exceed income ($%s) by $%s this month.",
                        totalExpenses.setScale(2, RoundingMode.HALF_UP),
                        totalIncome.setScale(2, RoundingMode.HALF_UP),
                        totalExpenses.subtract(totalIncome).setScale(2, RoundingMode.HALF_UP)),
                    "Savings",
                    profile.getSavingsTarget(),
                    savingsRate,
                    "/profile"
                ));
            } else if (savingsRate.compareTo(profile.getSavingsTarget()) < 0) {
                nudges.add(new SpendingNudgeDto(
                    "SAVINGS_RATE_DRIFT",
                    "MEDIUM",
                    "Savings Rate Off Target",
                    String.format("Current monthly savings rate is %s%%, which is below your target of %s%%.",
                        savingsRate.toPlainString(), profile.getSavingsTarget().toPlainString()),
                    "Savings",
                    profile.getSavingsTarget(),
                    savingsRate,
                    "/profile"
                ));
            }
        }

        // 3. Subscription alerts (price change or unused)
        if (subscriptionService != null) {
            List<SubscriptionDto> alerts = subscriptionService.getActiveAlerts(userId);
            for (SubscriptionDto sub : alerts) {
                if (sub.isPriceChangeAlert()) {
                    nudges.add(new SpendingNudgeDto(
                        "SUBSCRIPTION_ALERT",
                        "MEDIUM",
                        sub.getName() + " Price Change Detected",
                        sub.getPriceChangeMessage(),
                        sub.getCategory(),
                        sub.getPreviousAmount(),
                        sub.getNewAmount(),
                        "/subscriptions"
                    ));
                }
                if (sub.isUnusedAlert()) {
                    nudges.add(new SpendingNudgeDto(
                        "SUBSCRIPTION_ALERT",
                        "INFO",
                        sub.getName() + " Inactive Subscription",
                        sub.getUnusedMessage(),
                        sub.getCategory(),
                        sub.getAmount(),
                        BigDecimal.valueOf(sub.getDaysInactive()),
                        "/subscriptions"
                    ));
                }
            }
        }

        return nudges;
    }

    private void validatePeriod(int month, int year) {
        if (month < 1 || month > 12 || year < 2000 || year > 9999) {
            throw new com.financecoach.exception.BadRequestException("Month or year is invalid");
        }
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
        validatePeriod(targetMonth, targetYear);

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

