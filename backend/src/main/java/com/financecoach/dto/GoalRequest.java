package com.financecoach.dto;

import com.financecoach.entity.GoalPriority;
import com.financecoach.entity.GoalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalRequest {
    @NotBlank @Size(max=150) private String name;
    @NotNull @DecimalMin(value="0.01") private BigDecimal targetAmount;
    @NotNull @DecimalMin(value="0.0", inclusive=true) private BigDecimal currentAmount = BigDecimal.ZERO;
    @NotNull private LocalDate targetDate;
    @NotNull private GoalPriority priority = GoalPriority.MEDIUM;
    private GoalStatus status = GoalStatus.IN_PROGRESS;
    public String getName(){return name;} public void setName(String v){name=v;} public BigDecimal getTargetAmount(){return targetAmount;} public void setTargetAmount(BigDecimal v){targetAmount=v;} public BigDecimal getCurrentAmount(){return currentAmount;} public void setCurrentAmount(BigDecimal v){currentAmount=v;} public LocalDate getTargetDate(){return targetDate;} public void setTargetDate(LocalDate v){targetDate=v;} public GoalPriority getPriority(){return priority;} public void setPriority(GoalPriority v){priority=v;} public GoalStatus getStatus(){return status;} public void setStatus(GoalStatus v){status=v;}
}
