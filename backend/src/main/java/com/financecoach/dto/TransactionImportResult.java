package com.financecoach.dto;

import java.util.ArrayList;
import java.util.List;

public class TransactionImportResult {
    private int importedCount = 0;
    private int skippedDuplicates = 0;
    private int totalParsed = 0;
    private List<String> errors = new ArrayList<>();
    private List<TransactionResponse> transactions = new ArrayList<>();

    public TransactionImportResult() {
    }

    public TransactionImportResult(int importedCount, int skippedDuplicates, int totalParsed) {
        this.importedCount = importedCount;
        this.skippedDuplicates = skippedDuplicates;
        this.totalParsed = totalParsed;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getSkippedDuplicates() {
        return skippedDuplicates;
    }

    public void setSkippedDuplicates(int skippedDuplicates) {
        this.skippedDuplicates = skippedDuplicates;
    }

    public int getTotalParsed() {
        return totalParsed;
    }

    public void setTotalParsed(int totalParsed) {
        this.totalParsed = totalParsed;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<TransactionResponse> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }
}
