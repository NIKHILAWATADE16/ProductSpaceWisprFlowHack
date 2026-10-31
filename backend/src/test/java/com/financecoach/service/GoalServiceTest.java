package com.financecoach.service;

import com.financecoach.dto.GoalDto;
import com.financecoach.entity.FinancialGoal;
import com.financecoach.entity.GoalPriority;
import com.financecoach.entity.GoalStatus;
import com.financecoach.entity.User;
import com.financecoach.repository.FinancialGoalRepository;
import com.financecoach.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest {

    @Mock
    private FinancialGoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    private GoalService goalService;

    @BeforeEach
    void setUp() {
        goalService = new GoalService(goalRepository, userRepository);
    }

    @Test
    public void testRequiredMonthlySavingsForFutureGoal() {
        User user = new User("Alex", "alex@test.com", "pass");
        user.setId(1L);

        // Goal: Target 120,000, Saved 20,000, Remaining 100,000, 10 months away
        LocalDate futureDate = LocalDate.now().plusMonths(10);
        FinancialGoal goal = new FinancialGoal(
            user, "Emergency Fund", new BigDecimal("120000.00"), new BigDecimal("20000.00"),
            futureDate, GoalPriority.HIGH, GoalStatus.IN_PROGRESS
        );
        goal.setId(1L);

        GoalDto dto = goalService.dto(goal);

        assertEquals(new BigDecimal("100000.00"), dto.getRemainingAmount());
        assertFalse(dto.isCompleted());
        assertFalse(dto.isOverdue());
        assertTrue(dto.getMonthlySavingsRequired().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void testCompletedGoalRequiresZeroMonthlySavings() {
        User user = new User("Alex", "alex@test.com", "pass");
        user.setId(1L);

        // Target reached: 50,000 / 50,000
        FinancialGoal goal = new FinancialGoal(
            user, "Laptop Purchase", new BigDecimal("50000.00"), new BigDecimal("50000.00"),
            LocalDate.now().plusMonths(2), GoalPriority.MEDIUM, GoalStatus.COMPLETED
        );
        goal.setId(2L);

        GoalDto dto = goalService.dto(goal);

        assertEquals(BigDecimal.ZERO.setScale(2), dto.getRemainingAmount());
        assertTrue(dto.isCompleted());
        assertEquals(BigDecimal.ZERO.setScale(2), dto.getMonthlySavingsRequired());
    }

    @Test
    public void testOverdueGoalRequiresFullRemainingAmount() {
        User user = new User("Alex", "alex@test.com", "pass");
        user.setId(1L);

        // Target date in the past
        LocalDate pastDate = LocalDate.now().minusMonths(2);
        FinancialGoal goal = new FinancialGoal(
            user, "Vacation Fund", new BigDecimal("40000.00"), new BigDecimal("10000.00"),
            pastDate, GoalPriority.LOW, GoalStatus.IN_PROGRESS
        );
        goal.setId(3L);

        GoalDto dto = goalService.dto(goal);

        assertTrue(dto.isOverdue());
        assertFalse(dto.isCompleted());
        assertEquals(new BigDecimal("30000.00"), dto.getRemainingAmount());
        assertEquals(new BigDecimal("30000.00"), dto.getMonthlySavingsRequired());
    }
}

