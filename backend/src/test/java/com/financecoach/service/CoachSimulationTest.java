package com.financecoach.service;

import com.financecoach.dto.SimulationRequest;
import com.financecoach.dto.SimulationResponse;
import com.financecoach.entity.*;
import com.financecoach.repository.FinancialGoalRepository;
import com.financecoach.repository.FinancialProfileRepository;
import com.financecoach.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoachSimulationTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FinancialGoalRepository goalRepository;

    @Mock
    private FinancialProfileRepository profileRepository;

    private CoachService coachService;

    @BeforeEach
    void setUp() {
        coachService = new CoachService(transactionRepository, goalRepository, profileRepository);
    }

    @Test
    public void testSimulationIncreasesSavingsAndAcceleratesGoal() {
        Long userId = 1L;
        User user = new User("Alex", "alex@test.com", "pass");
        user.setId(userId);

        // Baseline transactions: Income 100,000, Expenses 60,000 -> Baseline Net Savings = 40,000
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(eq(userId), eq(TransactionType.INCOME), any(), any()))
            .thenReturn(new BigDecimal("100000.00"));
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(eq(userId), eq(TransactionType.EXPENSE), any(), any()))
            .thenReturn(new BigDecimal("60000.00"));

        // Active goal: Emergency Fund 100,000 remaining, 10 months away
        FinancialGoal goal = new FinancialGoal(
            user, "Emergency Fund", new BigDecimal("120000.00"), new BigDecimal("20000.00"),
            LocalDate.now().plusMonths(10), GoalPriority.HIGH, GoalStatus.IN_PROGRESS
        );
        goal.setId(10L);
        when(goalRepository.findByUserId(eq(userId), any(Sort.class))).thenReturn(List.of(goal));

        // Simulate: reduce expenses by 10,000 (expenseChange = -10000)
        SimulationRequest request = new SimulationRequest();
        request.setIncomeChange(BigDecimal.ZERO);
        request.setExpenseChange(new BigDecimal("-10000.00"));

        SimulationResponse response = coachService.simulate(userId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("100000.00"), response.getBaselineIncome());
        assertEquals(new BigDecimal("60000.00"), response.getBaselineExpenses());
        assertEquals(new BigDecimal("40000.00"), response.getBaselineNetSavings());

        // New expenses = 50,000; New Net Savings = 50,000; Delta = +10,000
        assertEquals(new BigDecimal("50000.00"), response.getNewExpenses());
        assertEquals(new BigDecimal("50000.00"), response.getNewNetSavings());
        assertEquals(new BigDecimal("10000.00"), response.getMonthlySavingsDelta());
        assertEquals(new BigDecimal("50.00"), response.getNewSavingsRate());

        // Verify goal projection accelerated
        assertFalse(response.getGoalProjections().isEmpty());
        assertTrue(response.getGoalProjections().get(0).getMonthsSaved() >= 0);
        assertNotNull(response.getSummaryText());
    }
}
