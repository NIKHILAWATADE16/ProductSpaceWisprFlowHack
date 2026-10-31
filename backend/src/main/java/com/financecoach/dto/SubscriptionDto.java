package com.financecoach.dto;

import com.financecoach.entity.BillingFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SubscriptionDto {
    private Long id;
    private String name;
    private BigDecimal amount;
    private BillingFrequency billingFrequency;
    private LocalDate nextPaymentDate;
    private String category;
    private boolean active;

    // Alert fields
    private boolean priceChangeAlert;
    private BigDecimal previousAmount;
    private BigDecimal newAmount;
    private String priceChangeMessage;

    private boolean unusedAlert;
    private long daysInactive;
    private String unusedMessage;

    public SubscriptionDto() {
    }

    public SubscriptionDto(Long id, String name, BigDecimal amount, BillingFrequency billingFrequency,
                           LocalDate nextPaymentDate, String category, boolean active) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.billingFrequency = billingFrequency;
        this.nextPaymentDate = nextPaymentDate;
        this.category = category;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BillingFrequency getBillingFrequency() {
        return billingFrequency;
    }

    public void setBillingFrequency(BillingFrequency billingFrequency) {
        this.billingFrequency = billingFrequency;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public void setNextPaymentDate(LocalDate nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPriceChangeAlert() {
        return priceChangeAlert;
    }

    public void setPriceChangeAlert(boolean priceChangeAlert) {
        this.priceChangeAlert = priceChangeAlert;
    }

    public BigDecimal getPreviousAmount() {
        return previousAmount;
    }

    public void setPreviousAmount(BigDecimal previousAmount) {
        this.previousAmount = previousAmount;
    }

    public BigDecimal getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(BigDecimal newAmount) {
        this.newAmount = newAmount;
    }

    public String getPriceChangeMessage() {
        return priceChangeMessage;
    }

    public void setPriceChangeMessage(String priceChangeMessage) {
        this.priceChangeMessage = priceChangeMessage;
    }

    public boolean isUnusedAlert() {
        return unusedAlert;
    }

    public void setUnusedAlert(boolean unusedAlert) {
        this.unusedAlert = unusedAlert;
    }

    public long getDaysInactive() {
        return daysInactive;
    }

    public void setDaysInactive(long daysInactive) {
        this.daysInactive = daysInactive;
    }

    public String getUnusedMessage() {
        return unusedMessage;
    }

    public void setUnusedMessage(String unusedMessage) {
        this.unusedMessage = unusedMessage;
    }
}
