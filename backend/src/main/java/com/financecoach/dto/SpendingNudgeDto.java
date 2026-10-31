package com.financecoach.dto;

import java.math.BigDecimal;

public class SpendingNudgeDto {
    private String type;      // BUDGET_OVERRUN, BUDGET_WARNING, SAVINGS_RATE_DRIFT, SUBSCRIPTION_ALERT
    private String severity;  // HIGH, MEDIUM, INFO
    private String title;
    private String message;
    private String category;
    private BigDecimal threshold;
    private BigDecimal current;
    private String actionUrl;

    public SpendingNudgeDto() {
    }

    public SpendingNudgeDto(String type, String severity, String title, String message,
                            String category, BigDecimal threshold, BigDecimal current, String actionUrl) {
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.category = category;
        this.threshold = threshold;
        this.current = current;
        this.actionUrl = actionUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public BigDecimal getCurrent() {
        return current;
    }

    public void setCurrent(BigDecimal current) {
        this.current = current;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
