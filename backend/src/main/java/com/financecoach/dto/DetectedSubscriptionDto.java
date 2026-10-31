package com.financecoach.dto;

import com.financecoach.entity.BillingFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DetectedSubscriptionDto {

    private String name;
    private BigDecimal amount;
    private BillingFrequency billingFrequency;
    private LocalDate nextPaymentDate;
    private String category;
    private int occurrenceCount;
    private String confidence;
    private boolean alreadyTracked;

    public DetectedSubscriptionDto() {
    }

    public DetectedSubscriptionDto(String name, BigDecimal amount, BillingFrequency billingFrequency,
                                   LocalDate nextPaymentDate, String category, int occurrenceCount,
                                   String confidence, boolean alreadyTracked) {
        this.name = name;
        this.amount = amount;
        this.billingFrequency = billingFrequency;
        this.nextPaymentDate = nextPaymentDate;
        this.category = category;
        this.occurrenceCount = occurrenceCount;
        this.confidence = confidence;
        this.alreadyTracked = alreadyTracked;
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

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(int occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public boolean isAlreadyTracked() {
        return alreadyTracked;
    }

    public void setAlreadyTracked(boolean alreadyTracked) {
        this.alreadyTracked = alreadyTracked;
    }
}

