package com.financecoach.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryDto {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private BigDecimal savingsRate; // Percentage e.g. 35.50
    private BigDecimal currentSavings;
    private BigDecimal emergencyFund;
    private BigDecimal monthlyFixedExpenses;
    private BigDecimal savingsTarget;
    private long goalCount;
    private long activeSubscriptionCount;
    private List<CategorySpendingDto> topSpendingCategories;

    public DashboardSummaryDto() {
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = netSavings;
    }

    public BigDecimal getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(BigDecimal savingsRate) {
        this.savingsRate = savingsRate;
    }

    public BigDecimal getCurrentSavings() {
        return currentSavings;
    }

    public void setCurrentSavings(BigDecimal currentSavings) {
        this.currentSavings = currentSavings;
    }

    public BigDecimal getEmergencyFund() {
        return emergencyFund;
    }

    public void setEmergencyFund(BigDecimal emergencyFund) {
        this.emergencyFund = emergencyFund;
    }

    public BigDecimal getMonthlyFixedExpenses() {
        return monthlyFixedExpenses;
    }

    public void setMonthlyFixedExpenses(BigDecimal monthlyFixedExpenses) {
        this.monthlyFixedExpenses = monthlyFixedExpenses;
    }

    public BigDecimal getSavingsTarget() {
        return savingsTarget;
    }

    public void setSavingsTarget(BigDecimal savingsTarget) {
        this.savingsTarget = savingsTarget;
    }

    public long getGoalCount() {
        return goalCount;
    }

    public void setGoalCount(long goalCount) {
        this.goalCount = goalCount;
    }

    public long getActiveSubscriptionCount() {
        return activeSubscriptionCount;
    }

    public void setActiveSubscriptionCount(long activeSubscriptionCount) {
        this.activeSubscriptionCount = activeSubscriptionCount;
    }

    public List<CategorySpendingDto> getTopSpendingCategories() {
        return topSpendingCategories;
    }

    public void setTopSpendingCategories(List<CategorySpendingDto> topSpendingCategories) {
        this.topSpendingCategories = topSpendingCategories;
    }
}
