package com.financecoach.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProfileUpdateRequest {

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly income cannot be negative")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Current savings is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Current savings cannot be negative")
    private BigDecimal currentSavings;

    @NotNull(message = "Emergency fund is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Emergency fund cannot be negative")
    private BigDecimal emergencyFund;

    @NotNull(message = "Monthly fixed expenses is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly fixed expenses cannot be negative")
    private BigDecimal monthlyFixedExpenses;

    @NotNull(message = "Savings target is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Savings target cannot be negative")
    private BigDecimal savingsTarget;

    public ProfileUpdateRequest() {
    }

    public ProfileUpdateRequest(BigDecimal monthlyIncome, BigDecimal currentSavings, BigDecimal emergencyFund,
                                BigDecimal monthlyFixedExpenses, BigDecimal savingsTarget) {
        this.monthlyIncome = monthlyIncome;
        this.currentSavings = currentSavings;
        this.emergencyFund = emergencyFund;
        this.monthlyFixedExpenses = monthlyFixedExpenses;
        this.savingsTarget = savingsTarget;
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
}
