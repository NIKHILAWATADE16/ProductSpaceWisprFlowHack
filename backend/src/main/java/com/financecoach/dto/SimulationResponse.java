package com.financecoach.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SimulationResponse {
    private BigDecimal baselineIncome;
    private BigDecimal baselineExpenses;
    private BigDecimal baselineNetSavings;
    private BigDecimal newIncome;
    private BigDecimal newExpenses;
    private BigDecimal newNetSavings;
    private BigDecimal monthlySavingsDelta;
    private BigDecimal newSavingsRate;
    private String summaryText;
    private List<GoalProjectionDto> goalProjections = new ArrayList<>();

    public SimulationResponse() {}

    public BigDecimal getBaselineIncome() { return baselineIncome; }
    public void setBaselineIncome(BigDecimal v) { baselineIncome = v; }
    public BigDecimal getBaselineExpenses() { return baselineExpenses; }
    public void setBaselineExpenses(BigDecimal v) { baselineExpenses = v; }
    public BigDecimal getBaselineNetSavings() { return baselineNetSavings; }
    public void setBaselineNetSavings(BigDecimal v) { baselineNetSavings = v; }
    public BigDecimal getNewIncome() { return newIncome; }
    public void setNewIncome(BigDecimal v) { newIncome = v; }
    public BigDecimal getNewExpenses() { return newExpenses; }
    public void setNewExpenses(BigDecimal v) { newExpenses = v; }
    public BigDecimal getNewNetSavings() { return newNetSavings; }
    public void setNewNetSavings(BigDecimal v) { newNetSavings = v; }
    public BigDecimal getMonthlySavingsDelta() { return monthlySavingsDelta; }
    public void setMonthlySavingsDelta(BigDecimal v) { monthlySavingsDelta = v; }
    public BigDecimal getNewSavingsRate() { return newSavingsRate; }
    public void setNewSavingsRate(BigDecimal v) { newSavingsRate = v; }
    public String getSummaryText() { return summaryText; }
    public void setSummaryText(String v) { summaryText = v; }
    public List<GoalProjectionDto> getGoalProjections() { return goalProjections; }
    public void setGoalProjections(List<GoalProjectionDto> v) { goalProjections = v; }
}
