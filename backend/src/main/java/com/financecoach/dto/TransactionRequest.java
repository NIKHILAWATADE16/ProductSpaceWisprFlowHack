package com.financecoach.dto;

import com.financecoach.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {
    @NotBlank @Size(max = 255) private String description;
    @NotNull @DecimalMin(value = "0.01") private BigDecimal amount;
    @NotNull private LocalDate transactionDate;
    @NotNull private TransactionType type;
    @NotBlank @Size(max = 100) private String category;
    @Size(max = 150) private String merchant;
    @Size(max = 50) private String paymentMethod;
    private boolean recurring;
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate value) { transactionDate = value; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType value) { type = value; }
    public String getCategory() { return category; }
    public void setCategory(String value) { category = value; }
    public String getMerchant() { return merchant; }
    public void setMerchant(String value) { merchant = value; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String value) { paymentMethod = value; }
    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean value) { recurring = value; }
}
