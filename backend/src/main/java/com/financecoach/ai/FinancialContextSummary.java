package com.financecoach.ai;

import java.math.BigDecimal;
import java.util.List;

public class FinancialContextSummary {

    private BigDecimal monthlyIncome = BigDecimal.ZERO;
    private BigDecimal monthlyExpenses = BigDecimal.ZERO;
    private BigDecimal netSavings = BigDecimal.ZERO;
    private BigDecimal savingsRate = BigDecimal.ZERO;
    private BigDecimal targetSavingsRate = BigDecimal.valueOf(20.00);
    private BigDecimal emergencyFundGoal = BigDecimal.ZERO;
    private BigDecimal totalMonthlySubscriptions = BigDecimal.ZERO;

    private List<CategoryDetail> topCategories = List.of();
    private List<GoalDetail> activeGoals = List.of();
    private List<BudgetDetail> activeBudgets = List.of();
    private List<SubscriptionDetail> activeSubscriptions = List.of();

    private String promptContextText = "";

    public static record CategoryDetail(String category, BigDecimal amount, double percentOfExpenses) {}

    public static record GoalDetail(
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            BigDecimal remainingAmount,
            String targetDate,
            long monthsRemaining,
            BigDecimal requiredMonthlySavings) {}

    public static record BudgetDetail(
            String category,
            BigDecimal monthlyLimit,
            BigDecimal currentSpent,
            double utilizationPercent,
            boolean exceeded) {}

    public static record SubscriptionDetail(
            String name,
            BigDecimal amount,
            String frequency,
            BigDecimal monthlyEquivalent) {}

    // Getters and Setters
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(BigDecimal monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }

    public BigDecimal getNetSavings() { return netSavings; }
    public void setNetSavings(BigDecimal netSavings) { this.netSavings = netSavings; }

    public BigDecimal getSavingsRate() { return savingsRate; }
    public void setSavingsRate(BigDecimal savingsRate) { this.savingsRate = savingsRate; }

    public BigDecimal getTargetSavingsRate() { return targetSavingsRate; }
    public void setTargetSavingsRate(BigDecimal targetSavingsRate) { this.targetSavingsRate = targetSavingsRate; }

    public BigDecimal getEmergencyFundGoal() { return emergencyFundGoal; }
    public void setEmergencyFundGoal(BigDecimal emergencyFundGoal) { this.emergencyFundGoal = emergencyFundGoal; }

    public BigDecimal getTotalMonthlySubscriptions() { return totalMonthlySubscriptions; }
    public void setTotalMonthlySubscriptions(BigDecimal totalMonthlySubscriptions) { this.totalMonthlySubscriptions = totalMonthlySubscriptions; }

    public List<CategoryDetail> getTopCategories() { return topCategories; }
    public void setTopCategories(List<CategoryDetail> topCategories) { this.topCategories = topCategories; }

    public List<GoalDetail> getActiveGoals() { return activeGoals; }
    public void setActiveGoals(List<GoalDetail> activeGoals) { this.activeGoals = activeGoals; }

    public List<BudgetDetail> getActiveBudgets() { return activeBudgets; }
    public void setActiveBudgets(List<BudgetDetail> activeBudgets) { this.activeBudgets = activeBudgets; }

    public List<SubscriptionDetail> getActiveSubscriptions() { return activeSubscriptions; }
    public void setActiveSubscriptions(List<SubscriptionDetail> activeSubscriptions) { this.activeSubscriptions = activeSubscriptions; }

    public String getPromptContextText() { return promptContextText; }
    public void setPromptContextText(String promptContextText) { this.promptContextText = promptContextText; }
}

