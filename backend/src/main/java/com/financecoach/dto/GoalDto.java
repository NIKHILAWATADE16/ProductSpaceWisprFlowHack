package com.financecoach.dto;

import com.financecoach.entity.GoalPriority;
import com.financecoach.entity.GoalStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalDto {
    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal monthlySavingsRequired;
    private LocalDate targetDate;
    private GoalPriority priority;
    private GoalStatus status;
    private double progressPercentage;
    private boolean overdue;
    private boolean completed;

    public GoalDto() {}

    public GoalDto(Long id, String name, BigDecimal targetAmount, BigDecimal currentAmount,
                   BigDecimal remainingAmount, BigDecimal monthlySavingsRequired, LocalDate targetDate,
                   GoalPriority priority, GoalStatus status, double progressPercentage,
                   boolean overdue, boolean completed) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.remainingAmount = remainingAmount;
        this.monthlySavingsRequired = monthlySavingsRequired;
        this.targetDate = targetDate;
        this.priority = priority;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.overdue = overdue;
        this.completed = completed;
    }

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal v) { targetAmount = v; }
    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal v) { currentAmount = v; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal v) { remainingAmount = v; }
    public BigDecimal getMonthlySavingsRequired() { return monthlySavingsRequired; }
    public void setMonthlySavingsRequired(BigDecimal v) { monthlySavingsRequired = v; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate v) { targetDate = v; }
    public GoalPriority getPriority() { return priority; }
    public void setPriority(GoalPriority v) { priority = v; }
    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus v) { status = v; }
    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double v) { progressPercentage = v; }
    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean v) { overdue = v; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean v) { completed = v; }
}
