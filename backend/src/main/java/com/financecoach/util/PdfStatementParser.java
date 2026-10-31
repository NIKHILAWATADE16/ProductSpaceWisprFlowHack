package com.financecoach.util;

import com.financecoach.entity.TransactionType;
import com.financecoach.exception.BadRequestException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfStatementParser {

    // Pattern matching the start of a transaction row (with optional serial number and flexible date formats)
    private static final Pattern ROW_START_PATTERN = Pattern.compile(
        "^\\s*(?:#?\\s*\\d{1,4}\\s+)?(\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{2,4}|\\d{1,4}[/-]\\d{1,2}[/-]\\d{1,4}|\\d{1,2}-(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*-\\d{2,4})\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Pattern matching opening balance line
    private static final Pattern OPENING_BALANCE_PATTERN = Pattern.compile(
        "(?i)opening\\s+balance\\s*[-:\\s]*\\s*([0-9,]+\\.[0-9]{2})"
    );

    // Pattern matching headers, footers, and disclaimer lines to filter out
    private static final Pattern NOISE_LINE_PATTERN = Pattern.compile(
        "(?i)^\\s*(?:account statement|savings account transactions|statement generated on|page\\s+\\d+|" +
        "account no\\.|account type|branch|account status|nominee registered|currency indian rupee|" +
        "micr\\s+\\d+|ifsc code|crn\\s+[a-z0-9]+|end of statement|important information|" +
        "commonly used narrations|hausla hai|visit www\\.|for assistance|remember!|never share|" +
        "kotak mahindra bank|registered office|particulars\\s+opening balance|#\\s+date\\s+description|" +
        "discrepancy in the statement|system generated report|rbi mandates|tds is applicable|goods and services tax).*$"
    );

    // Pattern matching reference numbers in descriptions
    private static final Pattern REF_PATTERN = Pattern.compile(
        "\\b(UPI-\\d+|NEFTINW-\\d+|MB-\\d+|NACHCR\\d+|REF\\d+|IMPS-\\d+)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Pattern matching amounts with optional CR/DR suffix
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "([0-9,]+\\.[0-9]{2})\\s*(CR|DR|Cr|Dr)?\\b"
    );

    public static List<ParsedStatementRow> parse(InputStream inputStream) {
        String pdfText;
        try {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                throw new BadRequestException("Uploaded PDF file is empty.");
            }

            try (PDDocument document = Loader.loadPDF(bytes)) {
                if (document.isEncrypted()) {
                    throw new BadRequestException("The PDF bank statement is password-protected. Please remove encryption or export as CSV.");
                }
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                pdfText = stripper.getText(document);
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Unable to read PDF bank statement. Error: " + e.getMessage() + ". Please upload a CSV instead.");
        }

        if (pdfText == null || pdfText.trim().isEmpty()) {
            throw new BadRequestException(
                "Unable to extract text from PDF bank statement. The document appears to be scanned or image-based. Please upload a CSV file instead."
            );
        }

        return parseText(pdfText);
    }

    public static List<ParsedStatementRow> parseText(String pdfText) {
        List<ParsedStatementRow> rows = new ArrayList<>();
        String[] lines = pdfText.split("\\r?\\n");

        BalanceTracker tracker = new BalanceTracker(null);
        StringBuilder currentBlock = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            // Check for Opening Balance
            Matcher obMatcher = OPENING_BALANCE_PATTERN.matcher(line);
            if (obMatcher.find()) {
                BigDecimal ob = StatementImportHelper.parseAmount(obMatcher.group(1));
                if (ob != null) {
                    tracker.setCurrentBalance(ob);
                }
                continue;
            }

            // Skip known noise lines
            if (NOISE_LINE_PATTERN.matcher(line).matches()) {
                continue;
            }

            // Check if this line starts a new transaction row
            if (ROW_START_PATTERN.matcher(line).find()) {
                if (!currentBlock.isEmpty()) {
                    ParsedStatementRow row = parseTransactionBlock(currentBlock.toString(), tracker);
                    if (row != null) {
                        rows.add(row);
                    }
                    currentBlock.setLength(0);
                }
                currentBlock.append(line);
            } else if (!currentBlock.isEmpty()) {
                // Continuation line for the active transaction block
                currentBlock.append(" ").append(line);
            }
        }

        // Flush the last transaction block
        if (!currentBlock.isEmpty()) {
            ParsedStatementRow row = parseTransactionBlock(currentBlock.toString(), tracker);
            if (row != null) {
                rows.add(row);
            }
        }

        if (rows.isEmpty()) {
            throw new BadRequestException(
                "Could not reliably extract transaction rows from this PDF bank statement format. Please verify the file or export and upload as CSV instead."
            );
        }

        return rows;
    }

    private static ParsedStatementRow parseTransactionBlock(String block, BalanceTracker tracker) {
        Matcher startMatcher = ROW_START_PATTERN.matcher(block);
        if (!startMatcher.find()) {
            return null;
        }

        String dateStr = startMatcher.group(1);
        LocalDate date = StatementImportHelper.parseDate(dateStr);
        if (date == null) {
            return null;
        }

        String remainder = block.substring(startMatcher.end()).trim();

        // Extract all amounts in the remainder
        Matcher amtMatcher = AMOUNT_PATTERN.matcher(remainder);
        List<AmountMatch> amounts = new ArrayList<>();
        while (amtMatcher.find()) {
            BigDecimal val = StatementImportHelper.parseAmount(amtMatcher.group(1));
            if (val != null) {
                amounts.add(new AmountMatch(val, amtMatcher.group(2), amtMatcher.start(), amtMatcher.end()));
            }
        }

        if (amounts.isEmpty()) {
            return null;
        }

        BigDecimal txAmount;
        BigDecimal balance = null;
        int descEndIndex;
        AmountMatch txMatch;

        if (amounts.size() >= 2) {
            // The last amount is typically the running balance
            AmountMatch last = amounts.get(amounts.size() - 1);
            AmountMatch secondLast = amounts.get(amounts.size() - 2);

            balance = last.amount;
            txAmount = secondLast.amount;
            txMatch = secondLast;
            descEndIndex = secondLast.start;
        } else {
            txMatch = amounts.get(0);
            txAmount = txMatch.amount;
            descEndIndex = txMatch.start;
        }

        if (txAmount == null || txAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String rawDesc = remainder.substring(0, descEndIndex).trim();

        // Extract payment reference (e.g., UPI-627312394276, NEFTINW-1734927899, MB-998312992146)
        String paymentMethod = "PDF Statement";
        Matcher refMatcher = REF_PATTERN.matcher(rawDesc);
        if (refMatcher.find()) {
            paymentMethod = refMatcher.group(1);
            rawDesc = rawDesc.replace(refMatcher.group(1), "").trim();
        }

        // Clean extra slashes/spaces in description
        rawDesc = rawDesc.replaceAll("\\s+", " ").trim();
        if (rawDesc.length() < 2) {
            rawDesc = "Transaction on " + date;
        }

        // Determine TransactionType
        TransactionType type = TransactionType.EXPENSE;
        BigDecimal previousBal = tracker.getCurrentBalance();

        boolean contiguousBalance = false;
        if (previousBal != null && balance != null) {
            BigDecimal diff = balance.subtract(previousBal).abs();
            if (diff.subtract(txAmount).abs().compareTo(BigDecimal.valueOf(0.05)) <= 0) {
                contiguousBalance = true;
                if (balance.compareTo(previousBal) > 0) {
                    type = TransactionType.INCOME;
                } else {
                    type = TransactionType.EXPENSE;
                }
            }
        }

        if (!contiguousBalance) {
            // 2. Explicit CR/DR tags
            if (txMatch.tag != null) {
                type = txMatch.tag.equalsIgnoreCase("CR") ? TransactionType.INCOME : TransactionType.EXPENSE;
            } else {
                // 3. Keyword based fallback
                String upper = (remainder + " " + rawDesc).toUpperCase();
                if (upper.contains("CREDIT") || upper.contains("CR-CREDIT") || upper.contains("SALARY") ||
                    upper.contains("REFUND") || upper.contains("REV-UPI") || upper.contains("REV-") ||
                    upper.contains("INT.PD") || upper.contains("INTEREST") || upper.contains("NEFT IN") ||
                    upper.contains("DEPOSIT") || upper.contains("REVERSAL")) {
                    type = TransactionType.INCOME;
                }
            }
        }

        if (balance != null) {
            tracker.setCurrentBalance(balance);
        }

        String merchant = extractCleanMerchant(rawDesc);

        return new ParsedStatementRow(date, rawDesc, txAmount, type, merchant, paymentMethod);
    }

    public static String extractCleanMerchant(String desc) {
        if (desc == null || desc.isBlank()) return "Unknown";
        String s = desc.trim();

        // 1. UPI format: UPI/MerchantName/Bank/Ref/...
        if (s.toUpperCase().startsWith("UPI/") || s.toUpperCase().startsWith("REV-UPI/")) {
            boolean isRev = s.toUpperCase().startsWith("REV-UPI/");
            String clean = s.replaceFirst("^(?i)(REV-)?UPI/", "");
            String[] parts = clean.split("/");
            if (parts.length > 0 && !parts[0].isBlank()) {
                String m = parts[0].trim();
                m = m.replaceAll("(?i)\\b(Sent using P|UPIIntent|NA)\\b", "").trim();
                return isRev ? m + " (Refund)" : m;
            }
        }

        // 2. Mobile Banking: MB:SENT TO [Name]
        if (s.toUpperCase().contains("MB:SENT TO")) {
            String m = s.replaceAll("(?i).*MB:SENT TO\\s+", "").trim();
            return m.replaceAll("(?i)\\s+MB-.*$", "").trim();
        }

        // 3. NEFT IN... [Beneficiary]
        if (s.toUpperCase().startsWith("NEFT IN")) {
            String clean = s.replaceAll("(?i)^NEFT\\s+IN[0-9A-Z]+\\s+", "").trim();
            clean = clean.replaceAll("(?i)\\s+(PVT\\s+LTD|LTD|BRA|LLC|INC).*$", " $1").trim();
            return clean.isEmpty() ? "NEFT Deposit" : clean;
        }

        // 4. Interest Paid: Int.Pd...
        if (s.toUpperCase().startsWith("INT.PD")) {
            return "Interest Paid";
        }

        // 5. NACH / PFMS
        if (s.toUpperCase().contains("PFMS")) {
            return "PFMS Government Credit";
        }

        // Fallback to CsvStatementParser.extractMerchant
        return CsvStatementParser.extractMerchant(desc);
    }

    private static class BalanceTracker {
        private BigDecimal currentBalance;

        public BalanceTracker(BigDecimal initial) {
            this.currentBalance = initial;
        }

        public BigDecimal getCurrentBalance() {
            return currentBalance;
        }

        public void setCurrentBalance(BigDecimal currentBalance) {
            this.currentBalance = currentBalance;
        }
    }

    private static class AmountMatch {
        final BigDecimal amount;
        final String tag;
        final int start;
        final int end;

        AmountMatch(BigDecimal amount, String tag, int start, int end) {
            this.amount = amount;
            this.tag = tag;
            this.start = start;
            this.end = end;
        }
    }
}
