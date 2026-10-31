package com.financecoach.dto;

import com.financecoach.entity.Transaction;
import com.financecoach.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private TransactionType type;
    private String category;
    private String merchant;
    private String paymentMethod;
    private boolean recurring;
    private LocalDateTime createdAt;

    public TransactionResponse() {
    }

    public TransactionResponse(Long id, String description, BigDecimal amount, LocalDate transactionDate,
                               TransactionType type, String category, String merchant,
                               String paymentMethod, boolean recurring, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.type = type;
        this.category = category;
        this.merchant = merchant;
        this.paymentMethod = paymentMethod;
        this.recurring = recurring;
        this.createdAt = createdAt;
    }

    public static TransactionResponse fromEntity(Transaction entity) {
        return new TransactionResponse(
            entity.getId(),
            entity.getDescription(),
            entity.getAmount(),
            entity.getTransactionDate(),
            entity.getType(),
            entity.getCategory(),
            entity.getMerchant(),
            entity.getPaymentMethod(),
            entity.isRecurring(),
            entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
