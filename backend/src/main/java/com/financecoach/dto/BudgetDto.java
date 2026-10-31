package com.financecoach.dto;

import java.math.BigDecimal;
public class BudgetDto {
    private Long id; private String category; private BigDecimal monthlyLimit; private Integer month; private Integer year; private BigDecimal amountSpent; private BigDecimal utilization;
    public BudgetDto() {}
    public BudgetDto(Long id,String category,BigDecimal monthlyLimit,Integer month,Integer year,BigDecimal amountSpent,BigDecimal utilization){this.id=id;this.category=category;this.monthlyLimit=monthlyLimit;this.month=month;this.year=year;this.amountSpent=amountSpent;this.utilization=utilization;}
    public Long getId(){return id;} public void setId(Long v){id=v;} public String getCategory(){return category;} public void setCategory(String v){category=v;} public BigDecimal getMonthlyLimit(){return monthlyLimit;} public void setMonthlyLimit(BigDecimal v){monthlyLimit=v;} public Integer getMonth(){return month;} public void setMonth(Integer v){month=v;} public Integer getYear(){return year;} public void setYear(Integer v){year=v;} public BigDecimal getAmountSpent(){return amountSpent;} public void setAmountSpent(BigDecimal v){amountSpent=v;} public BigDecimal getSpent(){return amountSpent;} public BigDecimal getUtilization(){return utilization; } public void setUtilization(BigDecimal v){utilization=v;}
}
