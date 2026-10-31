package com.financecoach.dto;

import jakarta.validation.constraints.NotBlank;

public class SmsImportRequest {

    @NotBlank(message = "SMS text cannot be blank")
    private String smsText;

    public SmsImportRequest() {
    }

    public SmsImportRequest(String smsText) {
        this.smsText = smsText;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }
}

