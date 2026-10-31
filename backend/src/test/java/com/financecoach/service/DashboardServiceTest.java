package com.financecoach.service;

import com.financecoach.dto.CategorySpendingDto;
import com.financecoach.dto.DashboardSummaryDto;
import com.financecoach.dto.FinancialProfileDto;
import com.financecoach.entity.FinancialGoal;
import com.financecoach.entity.GoalStatus;
import com.financecoach.entity.Subscription;
import com.financecoach.entity.TransactionType;
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

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
            transactionRepository, profileService, goalRepository, subscriptionRepository
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

    private FinancialProfileDto profile() {
        return new FinancialProfileDto(
            1L, 7L, new BigDecimal("5000.00"), new BigDecimal("12000.00"),
            new BigDecimal("6000.00"), new BigDecimal("1500.00"),
            new BigDecimal("20000.00"), null
        );
    }
}
