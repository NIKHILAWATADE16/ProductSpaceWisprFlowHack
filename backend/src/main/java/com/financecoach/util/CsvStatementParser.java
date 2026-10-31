package com.financecoach.util;

import com.financecoach.entity.TransactionType;
import com.financecoach.exception.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class CsvStatementParser {

    public static List<ParsedStatementRow> parse(InputStream inputStream) {
        List<ParsedStatementRow> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setSkipHeaderRecord(true)
                .setAllowMissingColumnNames(true)
                .setIgnoreEmptyLines(true)
                .build();

            CSVParser parser = format.parse(reader);
            Map<String, Integer> headerMap = parser.getHeaderMap();

            if (headerMap == null || headerMap.isEmpty()) {
                throw new BadRequestException("CSV file contains no readable header row");
            }

            // Identify column positions
            Integer dateCol = findColumn(headerMap, "date", "txn date", "transaction date", "value date");
            Integer descCol = findColumn(headerMap, "description", "narration", "particulars", "details", "remarks", "memo");
            Integer amountCol = findColumn(headerMap, "amount", "transaction amount", "net amount");
            Integer debitCol = findColumn(headerMap, "debit", "withdrawal", "dr", "debit amount");
            Integer creditCol = findColumn(headerMap, "credit", "deposit", "cr", "credit amount");
            Integer typeCol = findColumn(headerMap, "type", "cr/dr", "dr/cr", "transaction type");
            Integer merchantCol = findColumn(headerMap, "merchant", "payee", "party");

            if (dateCol == null || descCol == null || (amountCol == null && debitCol == null && creditCol == null)) {
                throw new BadRequestException("CSV format not recognized. Required columns: Date, Description/Narration, and Amount or Debit/Credit.");
            }

            for (CSVRecord record : parser) {
                if (record.size() == 0) continue;

                String dateStr = getSafeValue(record, dateCol);
                LocalDate date = StatementImportHelper.parseDate(dateStr);
                if (date == null) {
                    continue; // Skip rows where date is not parseable (e.g. footer lines)
                }

                String description = getSafeValue(record, descCol);
                if (description == null || description.isBlank()) {
                    description = "Imported Transaction";
                }

                BigDecimal amount = null;
                TransactionType type = TransactionType.EXPENSE;

                if (debitCol != null && creditCol != null) {
                    String debitStr = getSafeValue(record, debitCol);
                    String creditStr = getSafeValue(record, creditCol);

                    BigDecimal debit = StatementImportHelper.parseAmount(debitStr);
                    BigDecimal credit = StatementImportHelper.parseAmount(creditStr);

                    if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0) {
                        amount = credit;
                        type = TransactionType.INCOME;
                    } else if (debit != null && debit.compareTo(BigDecimal.ZERO) > 0) {
                        amount = debit;
                        type = TransactionType.EXPENSE;
                    }
                }

                if (amount == null && amountCol != null) {
                    String rawAmount = getSafeValue(record, amountCol);
                    amount = StatementImportHelper.parseAmount(rawAmount);

                    if (rawAmount != null && rawAmount.startsWith("-")) {
                        type = TransactionType.EXPENSE;
                    } else if (typeCol != null) {
                        String rawType = getSafeValue(record, typeCol);
                        if (rawType != null) {
                            String t = rawType.trim().toUpperCase();
                            if (t.contains("CR") || t.contains("CREDIT") || t.contains("INCOME") || t.contains("DEP")) {
                                type = TransactionType.INCOME;
                            } else {
                                type = TransactionType.EXPENSE;
                            }
                        }
                    }
                }

                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                String merchant = merchantCol != null ? getSafeValue(record, merchantCol) : null;
                if (merchant == null || merchant.isBlank()) {
                    merchant = extractMerchant(description);
                }

                rows.add(new ParsedStatementRow(date, description, amount, type, merchant, "Imported"));
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse CSV bank statement: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            throw new BadRequestException("No valid transactions found in CSV statement. Please check date and amount columns.");
        }

        return rows;
    }

    private static Integer findColumn(Map<String, Integer> headerMap, String... candidates) {
        for (String candidate : candidates) {
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                if (entry.getKey() != null && entry.getKey().trim().equalsIgnoreCase(candidate)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static String getSafeValue(CSVRecord record, Integer colIndex) {
        if (colIndex != null && colIndex >= 0 && colIndex < record.size()) {
            String val = record.get(colIndex);
            return val != null ? val.trim() : null;
        }
        return null;
    }

    public static String extractMerchant(String description) {
        if (description == null || description.isBlank()) return null;
        // Strip common prefixes like UPI/, POS/, ACH/, NEFT/, IMPS/
        String cleaned = description.replaceAll("^(UPI/|POS/|ACH/|NEFT/|IMPS/|DEBIT CARD/|ATM/|PURCHASE/|TRF/)", "").trim();
        String[] parts = cleaned.split("[-/]");
        return parts[0].trim();
    }
}

