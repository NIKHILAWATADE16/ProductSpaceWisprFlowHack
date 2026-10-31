package com.financecoach.dto;

import java.math.BigDecimal;

public class CategorySpendingDto {
    private String category;
    private BigDecimal amount;
    private double percentage;
    private long transactionCount;

    public CategorySpendingDto() {
    }

    public CategorySpendingDto(String category, BigDecimal amount, double percentage, long transactionCount) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.transactionCount = transactionCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }
}

