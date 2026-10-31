package com.financecoach.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeterministicCoachFallbackTest {

    private DeterministicCoachFallback fallback;
    private FinancialContextSummary summary;

    @BeforeEach
    void setUp() {
        fallback = new DeterministicCoachFallback();
        summary = new FinancialContextSummary();
        summary.setMonthlyIncome(new BigDecimal("5000.00"));
        summary.setMonthlyExpenses(new BigDecimal("3500.00"));
        summary.setNetSavings(new BigDecimal("1500.00"));
        summary.setSavingsRate(new BigDecimal("30.00"));
        summary.setTargetSavingsRate(new BigDecimal("20.00"));

        summary.setTopCategories(List.of(
                new FinancialContextSummary.CategoryDetail("Dining Out", new BigDecimal("750.00"), 21.4),
                new FinancialContextSummary.CategoryDetail("Groceries", new BigDecimal("500.00"), 14.3)
        ));

        summary.setActiveGoals(List.of(
                new FinancialContextSummary.GoalDetail("Emergency Fund", new BigDecimal("10000.00"),
                        new BigDecimal("6000.00"), new BigDecimal("4000.00"), "2025-08-31", 6, new BigDecimal("666.67"))
        ));

        summary.setActiveSubscriptions(List.of(
                new FinancialContextSummary.SubscriptionDetail("Netflix", new BigDecimal("15.49"), "MONTHLY", new BigDecimal("15.49"))
        ));
        summary.setTotalMonthlySubscriptions(new BigDecimal("15.49"));
    }

    @Test
    void coachResponsesUseIndianRupeeSymbol() {
        String response = fallback.generateFallback(summary, "Where is my spending highest?");

        assertTrue(response.contains("₹3,500.00"));
        assertTrue(response.contains("₹5,000.00"));
        assertFalse(response.contains("$"));
    }

    @Test
    void generatesSpendingAdvice() {
        String res = fallback.generateFallback(summary, "Where is my spending highest?");
        assertNotNull(res);
        assertTrue(res.contains("Dining Out"));
        assertTrue(res.contains("750.00"));
    }

    @Test
    void generatesSavingsAdvice() {
        String res = fallback.generateFallback(summary, "How can I improve my savings rate?");
        assertNotNull(res);
        assertTrue(res.contains("30.00%"));
    }

    @Test
    void generatesGoalsAdvice() {
        String res = fallback.generateFallback(summary, "What should I focus on this month for my goals?");
        assertNotNull(res);
        assertTrue(res.contains("Emergency Fund"));
        assertTrue(res.contains("666.67"));
    }

    @Test
    void generatesSubscriptionAdvice() {
        String res = fallback.generateFallback(summary, "What subscriptions am I paying for?");
        assertNotNull(res);
        assertTrue(res.contains("Netflix"));
        assertTrue(res.contains("15.49"));
    }

    @Test
    void generatesGeneralSnapshot() {
        String res = fallback.generateFallback(summary, "Give me an overview");
        assertNotNull(res);
        assertTrue(res.contains("Monthly Income"));
        assertTrue(res.contains("₹5,000.00"));
    }
}

