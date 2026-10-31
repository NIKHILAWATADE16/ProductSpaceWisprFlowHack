package com.financecoach.util;

import com.financecoach.entity.TransactionType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatementParserTest {

    @Test
    void testCsvStatementParsing() {
        String csv = "Date,Description,Amount,Type\n" +
            "2026-10-15,Uber ride,450.00,EXPENSE\n" +
            "2026-10-16,Salary from Acme Corp,120000.00,INCOME\n" +
            "2026-10-17,Swiggy dinner,650.50,EXPENSE\n";

        InputStream in = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        List<ParsedStatementRow> rows = CsvStatementParser.parse(in);

        assertEquals(3, rows.size());
        assertEquals(LocalDate.of(2026, 10, 15), rows.get(0).getDate());
        assertEquals("Uber ride", rows.get(0).getDescription());
        assertEquals(new BigDecimal("450.00"), rows.get(0).getAmount());
        assertEquals(TransactionType.EXPENSE, rows.get(0).getType());

        assertEquals(LocalDate.of(2026, 10, 16), rows.get(1).getDate());
        assertEquals(TransactionType.INCOME, rows.get(1).getType());
    }

    @Test
    void testComputeImportHashConsistency() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        BigDecimal amount = new BigDecimal("450.00");
        String desc = "Uber ride";

        String hash1 = StatementImportHelper.computeImportHash(1L, date, amount, desc);
        String hash2 = StatementImportHelper.computeImportHash(1L, date, amount, desc);
        String hashDifferent = StatementImportHelper.computeImportHash(2L, date, amount, desc);

        assertNotNull(hash1);
        assertEquals(hash1, hash2, "Identical inputs must yield identical hash");
        assertNotEquals(hash1, hashDifferent, "Different user ID must yield different hash");
    }

    @Test
    void testPdfStatementParsing() throws Exception {
        // Generate a valid text-based bank statement in memory with PDFBox 3
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
            doc.save(new java.io.File("/tmp/bank_statement.pdf"));
        }

        InputStream pdfIn = new ByteArrayInputStream(out.toByteArray());
        List<ParsedStatementRow> rows = PdfStatementParser.parse(pdfIn);

        assertNotNull(rows);
        assertFalse(rows.isEmpty());
        assertTrue(rows.stream().anyMatch(r -> r.getAmount().compareTo(new BigDecimal("499.00")) == 0));
        assertTrue(rows.stream().anyMatch(r -> r.getType() == TransactionType.INCOME));
    }
}
