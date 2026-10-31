package com.financecoach.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FinancialProfileDto {
    private Long id;
    private Long userId;
    private BigDecimal monthlyIncome;
    private BigDecimal currentSavings;
    private BigDecimal emergencyFund;
    private BigDecimal monthlyFixedExpenses;
    private BigDecimal savingsTarget;
    private LocalDateTime updatedAt;

    public FinancialProfileDto() {
    }

    public FinancialProfileDto(Long id, Long userId, BigDecimal monthlyIncome, BigDecimal currentSavings,
                               BigDecimal emergencyFund, BigDecimal monthlyFixedExpenses,
                               BigDecimal savingsTarget, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.monthlyIncome = monthlyIncome;
        this.currentSavings = currentSavings;
        this.emergencyFund = emergencyFund;
        this.monthlyFixedExpenses = monthlyFixedExpenses;
        this.savingsTarget = savingsTarget;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

