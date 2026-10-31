package com.financecoach.dto;
import jakarta.validation.constraints.NotNull; import java.math.BigDecimal;
public class SimulationRequest { @NotNull private BigDecimal incomeChange=BigDecimal.ZERO; @NotNull private BigDecimal expenseChange=BigDecimal.ZERO; public BigDecimal getIncomeChange(){return incomeChange;} public void setIncomeChange(BigDecimal v){incomeChange=v;} public BigDecimal getExpenseChange(){return expenseChange;} public void setExpenseChange(BigDecimal v){expenseChange=v;} }
