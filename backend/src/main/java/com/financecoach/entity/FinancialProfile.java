package com.financecoach.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_profiles", indexes = {
    @Index(name = "idx_financial_profiles_user_id", columnList = "user_id", unique = true)
})
public class FinancialProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly income cannot be negative")
    @Column(name = "monthly_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome = BigDecimal.ZERO;

    @NotNull(message = "Current savings is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Current savings cannot be negative")
    @Column(name = "current_savings", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentSavings = BigDecimal.ZERO;

    @NotNull(message = "Emergency fund is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Emergency fund cannot be negative")
    @Column(name = "emergency_fund", nullable = false, precision = 15, scale = 2)
    private BigDecimal emergencyFund = BigDecimal.ZERO;

    @NotNull(message = "Monthly fixed expenses is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly fixed expenses cannot be negative")
    @Column(name = "monthly_fixed_expenses", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyFixedExpenses = BigDecimal.ZERO;

    @NotNull(message = "Savings target is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Savings target cannot be negative")
    @Column(name = "savings_target", nullable = false, precision = 15, scale = 2)
    private BigDecimal savingsTarget = BigDecimal.ZERO;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FinancialProfile() {
    }

    public FinancialProfile(User user, BigDecimal monthlyIncome, BigDecimal currentSavings,
                            BigDecimal emergencyFund, BigDecimal monthlyFixedExpenses, BigDecimal savingsTarget) {
        this.user = user;
        this.monthlyIncome = monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO;
        this.currentSavings = currentSavings != null ? currentSavings : BigDecimal.ZERO;
        this.emergencyFund = emergencyFund != null ? emergencyFund : BigDecimal.ZERO;
        this.monthlyFixedExpenses = monthlyFixedExpenses != null ? monthlyFixedExpenses : BigDecimal.ZERO;
        this.savingsTarget = savingsTarget != null ? savingsTarget : BigDecimal.ZERO;
    }

    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

