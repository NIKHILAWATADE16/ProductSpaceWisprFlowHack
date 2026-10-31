package com.financecoach.dto;

import com.financecoach.entity.GoalPriority;
import com.financecoach.entity.GoalStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalDto {
    private Long id; private String name; private BigDecimal targetAmount; private BigDecimal currentAmount; private BigDecimal monthlySavingsRequired; private LocalDate targetDate; private GoalPriority priority; private GoalStatus status;
    public GoalDto() {}
    public GoalDto(Long id,String name,BigDecimal targetAmount,BigDecimal currentAmount,BigDecimal monthlySavingsRequired,LocalDate targetDate,GoalPriority priority,GoalStatus status){this.id=id;this.name=name;this.targetAmount=targetAmount;this.currentAmount=currentAmount;this.monthlySavingsRequired=monthlySavingsRequired;this.targetDate=targetDate;this.priority=priority;this.status=status;}
    public Long getId(){return id;} public void setId(Long v){id=v;} public String getName(){return name;} public void setName(String v){name=v;} public BigDecimal getTargetAmount(){return targetAmount;} public void setTargetAmount(BigDecimal v){targetAmount=v;} public BigDecimal getCurrentAmount(){return currentAmount;} public void setCurrentAmount(BigDecimal v){currentAmount=v;} public BigDecimal getMonthlySavingsRequired(){return monthlySavingsRequired;} public void setMonthlySavingsRequired(BigDecimal v){monthlySavingsRequired=v;} public LocalDate getTargetDate(){return targetDate;} public void setTargetDate(LocalDate v){targetDate=v;} public GoalPriority getPriority(){return priority;} public void setPriority(GoalPriority v){priority=v;} public GoalStatus getStatus(){return status;} public void setStatus(GoalStatus v){status=v;}
}
