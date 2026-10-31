package com.financecoach.util;

import com.financecoach.entity.TransactionType;
import com.financecoach.exception.BadRequestException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsStatementParser {

    // Pattern matching amounts specifically linked to transaction verbs (avoids picking up account balance)
    private static final Pattern ACTION_AMOUNT_PATTERN = Pattern.compile(
            "(?:debited\\s+(?:by|for|with)?|spent|charged|paid|sent|withdrawn|credited\\s+(?:by|for|with|:)?|received|refund\\s+of)\\s*(?:rs\\.?|inr|usd|\\$)?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)|(?:rs\\.?|inr|usd|\\$)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)(?=\\s+(?:debited|spent|charged|paid|sent|credited|withdrawn))",
            Pattern.CASE_INSENSITIVE
    );

    // Fallback amount pattern: amounts with currency symbol that are not preceded by bal/balance
    private static final Pattern GENERAL_AMOUNT_PATTERN = Pattern.compile(
            "(?<!(?:bal|balance|avl bal|avl\\s|a/c|acct)\\s{0,5})(?:rs\\.?|inr|usd|\\$)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)|([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:rs\\.?|inr|usd|\\$)",
            Pattern.CASE_INSENSITIVE
    );

    // Regex to detect transaction type
    private static final Pattern CREDIT_PATTERN = Pattern.compile(
            "\\b(credited|credit|received|refund|refunded|deposited|salary)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DEBIT_PATTERN = Pattern.compile(
            "\\b(debited|debit|spent|charged|paid|sent|withdrawn|purchase|purchase of)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Regex to extract date: yyyy-MM-dd, dd/MM/yyyy, dd-MM-yyyy
    private static final Pattern NUMERIC_DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,4}[/-]\\d{1,2}[/-]\\d{1,4})\\b"
    );

    // Regex to extract text dates: 15-Oct-2026, 12 Oct 26, 10-Nov-2026
    private static final Pattern TEXT_DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,2}[-\\s/](?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[-\\s/](?:\\d{4}|\\d{2}))\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Regex to extract merchant/recipient following 'at', 'to', 'from', 'towards', 'vpa', 'info:'
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
            "(?:\\bat\\b|\\bto\\b|\\bfrom\\b|\\btowards\\b|\\bvpa\\b|\\binfo:\\b)\\s+([A-Za-z0-9][A-Za-z0-9\\s\\.\\*\\#\\-]{1,35}?)(?=\\s+on|\\s+ref|\\s+avl|\\s+via|\\s+bal|\\s+ending|\\s+upi|\\.|,|$|\\n)",
            Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter[] TEXT_DATE_FORMATTERS = new DateTimeFormatter[] {
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d-MMM-[uuuu][uu]").toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM [uuuu][uu]").toFormatter(Locale.ENGLISH),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d/MMM/[uuuu][uu]").toFormatter(Locale.ENGLISH)
    };

    public static List<ParsedStatementRow> parse(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new BadRequestException("Unable to read SMS statement text: " + e.getMessage());
        }
        return parseText(sb.toString());
    }

    public static List<ParsedStatementRow> parseText(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new BadRequestException("SMS text content is empty.");
        }

        List<ParsedStatementRow> rows = new ArrayList<>();
        // Split by lines or double line-breaks
        String[] chunks = rawText.split("\\n+");

        for (String chunk : chunks) {
            chunk = chunk.trim();
            if (chunk.length() < 12) continue; // Skip noise

            ParsedStatementRow row = parseSmsLine(chunk);
            if (row != null) {
                rows.add(row);
            }
        }

        if (rows.isEmpty()) {
            throw new BadRequestException("No valid transaction SMS patterns recognized in the provided text. Ensure messages include amounts, dates, and debit/credit indicators.");
        }

        return rows;
    }

    public static ParsedStatementRow parseSmsLine(String message) {
        if (message == null || message.isBlank()) return null;

        // 1. Extract Amount
        BigDecimal amount = extractAmount(message);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // 2. Extract Type
        TransactionType type = TransactionType.EXPENSE;
        if (CREDIT_PATTERN.matcher(message).find()) {
            type = TransactionType.INCOME;
        } else if (DEBIT_PATTERN.matcher(message).find()) {
            type = TransactionType.EXPENSE;
        }

        // 3. Extract Date
        LocalDate date = extractDate(message);
        if (date == null) {
            date = LocalDate.now();
        }

        // 4. Extract Merchant / Narration
        String merchant = null;
        Matcher mMatcher = MERCHANT_PATTERN.matcher(message);
        if (mMatcher.find()) {
            merchant = mMatcher.group(1).trim();
            merchant = merchant.replaceAll("(?i)\\b(ref|avl|bal|ending|a/c)\\b.*$", "").trim();
        }

        String description;
        if (merchant != null && !merchant.isBlank()) {
            description = (type == TransactionType.INCOME ? "Received from " : "Payment to ") + merchant;
        } else {
            // Fallback description from first 40 characters
            String clean = message.replaceAll("(?i)(dear\\s+user|your\\s+a/c|sbi|hdfc|icici|axis|bank)", "").trim();
            description = clean.length() > 40 ? clean.substring(0, 40) : clean;
            merchant = CsvStatementParser.extractMerchant(description);
        }

        return new ParsedStatementRow(date, description, amount, type, merchant, "SMS Transaction");
    }

    private static BigDecimal extractAmount(String message) {
        // First try action-linked amount
        Matcher actionMatcher = ACTION_AMOUNT_PATTERN.matcher(message);
        if (actionMatcher.find()) {
            String match = actionMatcher.group(1) != null ? actionMatcher.group(1) : actionMatcher.group(2);
            BigDecimal amt = StatementImportHelper.parseAmount(match);
            if (amt != null && amt.compareTo(BigDecimal.ZERO) > 0) {
                return amt;
            }
        }

        // Fallback to general amount
        Matcher generalMatcher = GENERAL_AMOUNT_PATTERN.matcher(message);
        if (generalMatcher.find()) {
            String match = generalMatcher.group(1) != null ? generalMatcher.group(1) : generalMatcher.group(2);
            return StatementImportHelper.parseAmount(match);
        }

        return null;
    }

    private static LocalDate extractDate(String text) {
        // Try numeric date (dd/MM/yyyy, yyyy-MM-dd, etc.)
        Matcher numMatcher = NUMERIC_DATE_PATTERN.matcher(text);
        while (numMatcher.find()) {
            LocalDate parsed = StatementImportHelper.parseDate(numMatcher.group(1));
            if (parsed != null && !parsed.isAfter(LocalDate.now().plusDays(30))) {
                return parsed;
            }
        }

        // Try text date (dd-MMM-yy, dd MMM yyyy, etc.)
        Matcher textMatcher = TEXT_DATE_PATTERN.matcher(text);
        if (textMatcher.find()) {
            String dateStr = textMatcher.group(1).trim();
            for (DateTimeFormatter fmt : TEXT_DATE_FORMATTERS) {
                try {
                    LocalDate parsed = LocalDate.parse(dateStr, fmt);
                    if (!parsed.isAfter(LocalDate.now().plusDays(30))) {
                        return parsed;
                    }
                } catch (DateTimeParseException ignored) {
                }
            }
        }

        return null;
    }
}
