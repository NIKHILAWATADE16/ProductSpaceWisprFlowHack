package com.financecoach.service;

import com.financecoach.entity.User;
import com.financecoach.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DemoDataSeederTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private FinancialProfileRepository profileRepo;

    @Mock
    private FinancialGoalRepository goalRepo;

    @Mock
    private BudgetRepository budgetRepo;

    @Mock
    private SubscriptionRepository subscriptionRepo;

    @Mock
    private TransactionRepository txRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DemoDataSeeder seeder;
    private User testUser;

    @BeforeEach
    void setUp() {
        seeder = new DemoDataSeeder(userRepo, profileRepo, goalRepo, budgetRepo, subscriptionRepo, txRepo, passwordEncoder);
        testUser = new User("Demo User", "demo@financecoach.com", "encoded");
        testUser.setId(10L);
    }

    @Test
    void seedsDemoDataSuccessfully() {
        when(userRepo.findByEmail("demo@financecoach.com")).thenReturn(Optional.of(testUser));
        when(profileRepo.findByUserId(10L)).thenReturn(Optional.empty());
        when(goalRepo.findByUserId(eq(10L), any(Sort.class))).thenReturn(Collections.emptyList());
        when(budgetRepo.findByUserId(10L)).thenReturn(Collections.emptyList());
        when(subscriptionRepo.findByUserId(eq(10L), any(Sort.class))).thenReturn(Collections.emptyList());

        Map<String, Object> res = seeder.seedDemoData("demo@financecoach.com", "Password123!", "Demo User", false);

        assertNotNull(res);
        assertEquals("SUCCESS", res.get("status"));
        assertEquals("demo@financecoach.com", res.get("email"));
        assertEquals(3, res.get("goalsCount"));
        assertEquals(4, res.get("budgetsCount"));
        assertEquals(4, res.get("subscriptionsCount"));

        verify(profileRepo).save(any());
        verify(goalRepo).saveAll(any());
        verify(budgetRepo).saveAll(any());
        verify(subscriptionRepo).saveAll(any());
    }
}
