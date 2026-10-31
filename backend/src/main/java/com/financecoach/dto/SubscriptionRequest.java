package com.financecoach.dto;

import com.financecoach.entity.BillingFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionRequest {
    @NotBlank @Size(max = 150) private String name;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    @NotNull private BillingFrequency billingFrequency;
    @NotNull private LocalDate nextPaymentDate;
    @NotBlank @Size(max = 100) private String category;

    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public BillingFrequency getBillingFrequency() { return billingFrequency; }
    public void setBillingFrequency(BillingFrequency value) { billingFrequency = value; }
    public LocalDate getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDate value) { nextPaymentDate = value; }
    public String getCategory() { return category; }
    public void setCategory(String value) { category = value; }
}
