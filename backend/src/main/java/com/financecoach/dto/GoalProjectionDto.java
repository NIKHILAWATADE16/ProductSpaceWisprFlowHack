package com.financecoach.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalProjectionDto {
    private Long goalId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal remainingAmount;
    private LocalDate baselineTargetDate;
    private LocalDate projectedCompletionDate;
    private long monthsRemainingBaseline;
    private long monthsRemainingProjected;
    private long monthsSaved;
    private String summary;

    public GoalProjectionDto() {}

    public GoalProjectionDto(Long goalId, String goalName, BigDecimal targetAmount, BigDecimal currentAmount,
                             BigDecimal remainingAmount, LocalDate baselineTargetDate, LocalDate projectedCompletionDate,
                             long monthsRemainingBaseline, long monthsRemainingProjected, long monthsSaved, String summary) {
        this.goalId = goalId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.remainingAmount = remainingAmount;
        this.baselineTargetDate = baselineTargetDate;
        this.projectedCompletionDate = projectedCompletionDate;
        this.monthsRemainingBaseline = monthsRemainingBaseline;
        this.monthsRemainingProjected = monthsRemainingProjected;
        this.monthsSaved = monthsSaved;
        this.summary = summary;
    }

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }
    public LocalDate getBaselineTargetDate() { return baselineTargetDate; }
    public void setBaselineTargetDate(LocalDate baselineTargetDate) { this.baselineTargetDate = baselineTargetDate; }
    public LocalDate getProjectedCompletionDate() { return projectedCompletionDate; }
    public void setProjectedCompletionDate(LocalDate projectedCompletionDate) { this.projectedCompletionDate = projectedCompletionDate; }
    public long getMonthsRemainingBaseline() { return monthsRemainingBaseline; }
    public void setMonthsRemainingBaseline(long monthsRemainingBaseline) { this.monthsRemainingBaseline = monthsRemainingBaseline; }
    public long getMonthsRemainingProjected() { return monthsRemainingProjected; }
    public void setMonthsRemainingProjected(long monthsRemainingProjected) { this.monthsRemainingProjected = monthsRemainingProjected; }
    public long getMonthsSaved() { return monthsSaved; }
    public void setMonthsSaved(long monthsSaved) { this.monthsSaved = monthsSaved; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
