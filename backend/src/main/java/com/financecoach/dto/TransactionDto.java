package com.financecoach.dto;

import com.financecoach.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDto {
    private Long id; private String description; private BigDecimal amount; private LocalDate transactionDate;
    private TransactionType type; private String category; private String merchant; private String paymentMethod; private boolean recurring;
    public TransactionDto() {}
    public TransactionDto(Long id, String description, BigDecimal amount, LocalDate transactionDate, TransactionType type, String category, String merchant, String paymentMethod, boolean recurring) {
        this.id=id; this.description=description; this.amount=amount; this.transactionDate=transactionDate; this.type=type; this.category=category; this.merchant=merchant; this.paymentMethod=paymentMethod; this.recurring=recurring;
    }
    public Long getId(){return id;} public void setId(Long v){id=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public LocalDate getTransactionDate(){return transactionDate;} public void setTransactionDate(LocalDate v){transactionDate=v;} public TransactionType getType(){return type;} public void setType(TransactionType v){type=v;} public String getCategory(){return category;} public void setCategory(String v){category=v;} public String getMerchant(){return merchant;} public void setMerchant(String v){merchant=v;} public String getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(String v){paymentMethod=v;} public boolean isRecurring(){return recurring;} public void setRecurring(boolean v){recurring=v;}
}
