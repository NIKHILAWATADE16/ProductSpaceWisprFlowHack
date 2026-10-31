package com.financecoach.util;

import com.financecoach.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ParsedStatementRow {
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private String merchant;
    private String paymentMethod;

    public ParsedStatementRow() {
    }

    public ParsedStatementRow(LocalDate date, String description, BigDecimal amount,
                              TransactionType type, String merchant, String paymentMethod) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.merchant = merchant;
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

