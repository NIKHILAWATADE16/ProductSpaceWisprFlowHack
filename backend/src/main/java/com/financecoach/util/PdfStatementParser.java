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

    // Regex pattern matching common bank statement line formats:
    // Group 1: Date (dd/mm/yyyy, dd-mm-yyyy, yyyy-mm-dd)
    // Group 2: Middle text (description/narration)
    // Group 3: Amount
    // Group 4: Optional Cr/Dr tag
    private static final Pattern LINE_PATTERN = Pattern.compile(
        "^\\s*(\\d{1,4}[/-]\\d{1,2}[/-]\\d{1,4})\\s+(.+?)\\s+([0-9,]+\\.[0-9]{2})\\s*(CR|DR|Cr|Dr)?\\s*$",
        Pattern.CASE_INSENSITIVE
    );

    // Fallback pattern where amount is earlier or separated
    private static final Pattern DATE_AMOUNT_EXTRACTOR = Pattern.compile(
        "(\\d{1,4}[/-]\\d{1,2}[/-]\\d{1,4})\\s+(.+?)\\s+([0-9,]+\\.[0-9]{2})"
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

        List<ParsedStatementRow> rows = new ArrayList<>();
        String[] lines = pdfText.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.length() < 10) continue;

            Matcher matcher = LINE_PATTERN.matcher(line);
            if (matcher.find()) {
                String dateStr = matcher.group(1);
                String descStr = matcher.group(2).trim();
                String amountStr = matcher.group(3);
                String crDrTag = matcher.group(4);

                LocalDate date = StatementImportHelper.parseDate(dateStr);
                BigDecimal amount = StatementImportHelper.parseAmount(amountStr);

                if (date != null && amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                    TransactionType type = TransactionType.EXPENSE;
                    if (crDrTag != null) {
                        String tag = crDrTag.toUpperCase();
                        if (tag.equals("CR")) {
                            type = TransactionType.INCOME;
                        }
                    } else if (descStr.toUpperCase().contains("CREDIT") || descStr.toUpperCase().contains("SALARY") || descStr.toUpperCase().contains("REFUND")) {
                        type = TransactionType.INCOME;
                    }

                    String merchant = CsvStatementParser.extractMerchant(descStr);
                    rows.add(new ParsedStatementRow(date, descStr, amount, type, merchant, "PDF Statement"));
                    continue;
                }
            }

            // Secondary fallback matcher
            Matcher fallbackMatcher = DATE_AMOUNT_EXTRACTOR.matcher(line);
            if (fallbackMatcher.find()) {
                String dateStr = fallbackMatcher.group(1);
                String descStr = fallbackMatcher.group(2).trim();
                String amountStr = fallbackMatcher.group(3);

                LocalDate date = StatementImportHelper.parseDate(dateStr);
                BigDecimal amount = StatementImportHelper.parseAmount(amountStr);

                if (date != null && amount != null && amount.compareTo(BigDecimal.ZERO) > 0 && descStr.length() > 2) {
                    TransactionType type = TransactionType.EXPENSE;
                    String upper = line.toUpperCase();
                    if (upper.contains(" CR") || upper.contains("CREDIT") || upper.contains("SALARY")) {
                        type = TransactionType.INCOME;
                    }
                    String merchant = CsvStatementParser.extractMerchant(descStr);
                    rows.add(new ParsedStatementRow(date, descStr, amount, type, merchant, "PDF Statement"));
                }
            }
        }

        if (rows.isEmpty()) {
            throw new BadRequestException(
                "Could not reliably extract transaction rows from this PDF bank statement format. Please verify the file or export and upload as CSV instead."
            );
        }

        return rows;
    }
}

