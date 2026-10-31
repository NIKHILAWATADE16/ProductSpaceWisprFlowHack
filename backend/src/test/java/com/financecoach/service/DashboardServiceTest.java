package com.financecoach.service;

import com.financecoach.dto.*;
import com.financecoach.entity.*;
import com.financecoach.repository.BudgetRepository;
import com.financecoach.repository.FinancialGoalRepository;
import com.financecoach.repository.SubscriptionRepository;
import com.financecoach.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FinancialProfileService profileService;

    @Mock
    private FinancialGoalRepository goalRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private SubscriptionService subscriptionService;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
            transactionRepository, profileService, goalRepository, subscriptionRepository,
            budgetRepository, subscriptionService
        );
    }

    @Test
    void calculatesMonthlyTotalsSavingsRateAndCategoryBreakdown() {
        Long userId = 7L;
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate endDate = LocalDate.of(2026, 2, 28);

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.INCOME, startDate, endDate
        )).thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.EXPENSE, startDate, endDate
        )).thenReturn(new BigDecimal("1750.00"));
        when(transactionRepository.findCategorySpendingSummary(
            userId, TransactionType.EXPENSE, startDate, endDate
        )).thenReturn(List.of(
            new Object[]{"Housing", new BigDecimal("1000.00"), 2L},
            new Object[]{"Food", new BigDecimal("750.00"), 5L}
        ));
        when(profileService.getProfileByUserId(userId)).thenReturn(profile());
        when(goalRepository.findByUserIdAndStatus(eq(userId), eq(GoalStatus.IN_PROGRESS), eq(org.springframework.data.domain.Sort.unsorted())))
            .thenReturn(List.of(new FinancialGoal()));
        when(subscriptionRepository.findByUserIdAndActive(eq(userId), eq(true), eq(org.springframework.data.domain.Sort.unsorted())))
            .thenReturn(List.of(new Subscription(), new Subscription()));

        DashboardSummaryDto summary = dashboardService.getDashboardSummary(userId, 2, 2026);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo("5000.00");
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo("1750.00");
        assertThat(summary.getNetSavings()).isEqualByComparingTo("3250.00");
        assertThat(summary.getSavingsRate()).isEqualByComparingTo("65.00");
        assertThat(summary.getGoalCount()).isEqualTo(1);
        assertThat(summary.getActiveSubscriptionCount()).isEqualTo(2);
        assertThat(summary.getTopSpendingCategories()).extracting(CategorySpendingDto::getCategory)
            .containsExactly("Housing", "Food");
        assertThat(summary.getTopSpendingCategories().get(0).getAmount()).isEqualByComparingTo("1000.00");
        assertThat(summary.getTopSpendingCategories().get(0).getPercentage()).isEqualTo(57.14);
        assertThat(summary.getTopSpendingCategories().get(1).getPercentage()).isEqualTo(42.86);
    }

    @Test
    void treatsMissingTotalsAsZeroAndDoesNotCalculateSavingsRateWithoutIncome() {
        Long userId = 11L;
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);

        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.INCOME, startDate, endDate
        )).thenReturn(null);
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.EXPENSE, startDate, endDate
        )).thenReturn(null);
        when(transactionRepository.findCategorySpendingSummary(
            userId, TransactionType.EXPENSE, startDate, endDate
        )).thenReturn(List.of());
        when(profileService.getProfileByUserId(userId)).thenReturn(profile());
        when(goalRepository.findByUserIdAndStatus(eq(userId), eq(GoalStatus.IN_PROGRESS), eq(org.springframework.data.domain.Sort.unsorted())))
            .thenReturn(List.of());
        when(subscriptionRepository.findByUserIdAndActive(eq(userId), eq(true), eq(org.springframework.data.domain.Sort.unsorted())))
            .thenReturn(List.of());

        DashboardSummaryDto summary = dashboardService.getDashboardSummary(userId, 3, 2026);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(summary.getNetSavings()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(summary.getTopSpendingCategories()).isEmpty();
    }

    @Test
    void generatesBudgetOverrunAndWarningNudges() {
        Long userId = 5L;
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        User u = new User("user@test.com", "pass", "User Test");
        Budget bDining = new Budget(u, "Dining", new BigDecimal("500.00"), 10, 2026);
        Budget bGroceries = new Budget(u, "Groceries", new BigDecimal("400.00"), 10, 2026);

        when(budgetRepository.findByUserIdAndMonthAndYear(userId, 10, 2026))
            .thenReturn(List.of(bDining, bGroceries));

        // Dining spent 620 (overrun)
        when(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndDateBetween(
            userId, TransactionType.EXPENSE, "Dining", startDate, endDate
        )).thenReturn(new BigDecimal("620.00"));

        // Groceries spent 350 (87.5% -> warning)
        when(transactionRepository.sumAmountByUserIdAndTypeAndCategoryAndDateBetween(
            userId, TransactionType.EXPENSE, "Groceries", startDate, endDate
        )).thenReturn(new BigDecimal("350.00"));

        when(profileService.getProfileByUserId(userId)).thenReturn(profile());

        List<SpendingNudgeDto> nudges = dashboardService.getSpendingNudges(userId, 10, 2026);

        assertNotNull(nudges);
        assertEquals(2, nudges.size());

        SpendingNudgeDto n1 = nudges.get(0);
        assertEquals("BUDGET_OVERRUN", n1.getType());
        assertEquals("HIGH", n1.getSeverity());
        assertTrue(n1.getTitle().contains("Dining Budget Exceeded"));

        SpendingNudgeDto n2 = nudges.get(1);
        assertEquals("BUDGET_WARNING", n2.getType());
        assertEquals("MEDIUM", n2.getSeverity());
        assertTrue(n2.getTitle().contains("Groceries Approaching Limit"));
    }

    @Test
    void generatesSavingsRateDriftAndSubscriptionNudges() {
        Long userId = 5L;
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        // Income 3000, Expense 2800 -> savings rate 6.67%, profile target is 20%
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.INCOME, startDate, endDate
        )).thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
            userId, TransactionType.EXPENSE, startDate, endDate
        )).thenReturn(new BigDecimal("2800.00"));

        FinancialProfileDto p = new FinancialProfileDto(
            1L, userId, new BigDecimal("3000.00"), new BigDecimal("1000.00"),
            new BigDecimal("500.00"), new BigDecimal("1000.00"),
            new BigDecimal("20.00"), null // target 20%
        );
        when(profileService.getProfileByUserId(userId)).thenReturn(p);

        SubscriptionDto subAlert = new SubscriptionDto();
        subAlert.setName("Netflix");
        subAlert.setCategory("Entertainment");
        subAlert.setPriceChangeAlert(true);
        subAlert.setPreviousAmount(new BigDecimal("10.99"));
        subAlert.setNewAmount(new BigDecimal("15.49"));
        subAlert.setPriceChangeMessage("Price increased from $10.99 to $15.49 (+4.50)");

        when(subscriptionService.getActiveAlerts(userId)).thenReturn(List.of(subAlert));

        List<SpendingNudgeDto> nudges = dashboardService.getSpendingNudges(userId, 10, 2026);

        assertEquals(2, nudges.size());

        SpendingNudgeDto savingsNudge = nudges.get(0);
        assertEquals("SAVINGS_RATE_DRIFT", savingsNudge.getType());
        assertEquals("MEDIUM", savingsNudge.getSeverity());

        SpendingNudgeDto subNudge = nudges.get(1);
        assertEquals("SUBSCRIPTION_ALERT", subNudge.getType());
        assertEquals("Netflix Price Change Detected", subNudge.getTitle());
    }

    private FinancialProfileDto profile() {
        return new FinancialProfileDto(
            1L, 7L, new BigDecimal("5000.00"), new BigDecimal("12000.00"),
            new BigDecimal("6000.00"), new BigDecimal("1500.00"),
            BigDecimal.ZERO, null
        );
    }
}
