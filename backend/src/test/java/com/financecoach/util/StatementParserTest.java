package com.financecoach.util;

import com.financecoach.entity.TransactionType;
import com.financecoach.exception.BadRequestException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class StatementParserTest {

    @Test
    public void testCsvStatementParsing() {
        String csv = "Date,Description,Amount,Type\n" +
            "2026-10-15,Uber ride,450.00,EXPENSE\n" +
            "2026-10-16,Salary from Acme Corp,120000.00,INCOME\n" +
            "2026-10-17,Swiggy dinner,650.50,EXPENSE\n";

        InputStream in = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        List<ParsedStatementRow> rows = CsvStatementParser.parse(in);

        Assertions.assertEquals(3, rows.size());
        Assertions.assertEquals(LocalDate.of(2026, 10, 15), rows.get(0).getDate());
        Assertions.assertEquals("Uber ride", rows.get(0).getDescription());
        Assertions.assertEquals(new BigDecimal("450.00"), rows.get(0).getAmount());
        Assertions.assertEquals(TransactionType.EXPENSE, rows.get(0).getType());

        Assertions.assertEquals(LocalDate.of(2026, 10, 16), rows.get(1).getDate());
        Assertions.assertEquals(TransactionType.INCOME, rows.get(1).getType());
    }

    @Test
    public void testCsvWithDebitCreditColumns() {
        String csv = "Txn Date,Particulars,Debit,Credit\n" +
            "15/10/2026,Grocery store,1200.00,\n" +
            "16/10/2026,Client deposit,,45000.00\n";

        InputStream in = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        List<ParsedStatementRow> rows = CsvStatementParser.parse(in);

        Assertions.assertEquals(2, rows.size());
        Assertions.assertEquals(new BigDecimal("1200.00"), rows.get(0).getAmount());
        Assertions.assertEquals(TransactionType.EXPENSE, rows.get(0).getType());
        Assertions.assertEquals(new BigDecimal("45000.00"), rows.get(1).getAmount());
        Assertions.assertEquals(TransactionType.INCOME, rows.get(1).getType());
    }

    @Test
    public void testComputeImportHashConsistency() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        BigDecimal amount = new BigDecimal("450.00");
        String desc = "Uber ride";

        String hash1 = StatementImportHelper.computeImportHash(1L, date, amount, desc);
        String hash2 = StatementImportHelper.computeImportHash(1L, date, amount, desc);
        String hashDifferent = StatementImportHelper.computeImportHash(2L, date, amount, desc);

        Assertions.assertNotNull(hash1);
        Assertions.assertEquals(hash1, hash2, "Identical inputs must yield identical hash");
        Assertions.assertNotEquals(hash1, hashDifferent, "Different user ID must yield different hash");
    }

    @Test
    public void testPdfStatementParsing() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Account Statement");
                stream.newLineAtOffset(0, -20);
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                stream.showText("2026-10-01 Netflix Subscription 499.00 DR");
                stream.newLineAtOffset(0, -20);
                stream.showText("2026-10-05 Salary Deposit 125000.00 CR");
                stream.endText();
            }
            doc.save(out);
        }

        InputStream pdfIn = new ByteArrayInputStream(out.toByteArray());
        List<ParsedStatementRow> rows = PdfStatementParser.parse(pdfIn);

        Assertions.assertNotNull(rows);
        Assertions.assertFalse(rows.isEmpty());
        Assertions.assertTrue(rows.stream().anyMatch(r -> r.getAmount().compareTo(new BigDecimal("499.00")) == 0));
        Assertions.assertTrue(rows.stream().anyMatch(r -> r.getType() == TransactionType.INCOME));
    }

    @Test
    public void testPdfEmptyOrCorruptedRejection() {
        InputStream emptyIn = new ByteArrayInputStream(new byte[0]);
        Assertions.assertThrows(BadRequestException.class, () -> PdfStatementParser.parse(emptyIn));

        InputStream corruptIn = new ByteArrayInputStream("Not a PDF content".getBytes(StandardCharsets.UTF_8));
        Assertions.assertThrows(BadRequestException.class, () -> PdfStatementParser.parse(corruptIn));
    }

    /**
     * Entry point allowing direct execution from IDEs without JUnit runner errors.
     */
    public static void main(String[] args) {
        System.out.println("Running StatementParserTest directly...");
        StatementParserTest test = new StatementParserTest();
        test.testCsvStatementParsing();
        test.testCsvWithDebitCreditColumns();
        test.testComputeImportHashConsistency();
        try {
            test.testPdfStatementParsing();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        test.testPdfEmptyOrCorruptedRejection();
        System.out.println("All StatementParserTest tests passed successfully!");
    }
}
