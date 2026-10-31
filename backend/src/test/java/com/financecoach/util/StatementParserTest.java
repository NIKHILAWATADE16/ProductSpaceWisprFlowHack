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
    public void testKotakMahindraMultiLineFormat() {
        String kotakText = """
            Account Statement
            30 Sep 2026 - 30 Oct 2026
            Yogini Santosh Gade
            Savings Account Transactions
            # Date Description Chq/Ref. No. Withdrawal (Dr.) Deposit (Cr.) Balance
            - - Opening Balance - - - 2,212.95
            1 30 Sep 2026 UPI/CMP PPI
            Wallet/YESB/627313781268/weWVlvq7x
            UPI-627312394276 200.00 2,012.95
            2 30 Sep 2026 UPI/COMPASS INDIA
            /UTIB/627354125082/151f
            UPI-627332966059 80.00 1,932.95
            4 30 Sep 2026 Int.Pd:3648704286:01-07-2026 to 30-09-2026 97.00 2,019.95
            6 01 Oct 2026 NACH-APB-CR-CREDIT THROUGH PFMS
            NACHCR011026002847 1,809.00 3,798.95
            13 02 Oct 2026 NEFT IN22627532707614 RANGAM INFOTECH
            PVT LTD BRA
            NEFTINW-1734927899 31,744.00 34,812.95
            14 03 Oct 2026 UPI/Taco Bell Ober/YESB/615262161084/Sent using P
            UPI-627674641429 114.00 34,698.95
            15 04 Oct 2026 MB:SENT TO ATHARV SHANMUKH DHUP MB-998312992146 430.00 34,268.95
            36 11 Oct 2026 REV-UPI/Maharashtra
            S/217799683128/COMPLAIN
            UPI-628461288204 375.00 32,143.95
            Statement Generated on 30 Oct 2026, 21:16 Page 1 of 6
            """;

        List<ParsedStatementRow> rows = PdfStatementParser.parseText(kotakText);
        Assertions.assertEquals(8, rows.size());

        // Row 1: 200.00 Expense (withdrawal)
        ParsedStatementRow r1 = rows.get(0);
        Assertions.assertEquals(LocalDate.of(2026, 9, 30), r1.getDate());
        Assertions.assertEquals(new BigDecimal("200.00"), r1.getAmount());
        Assertions.assertEquals(TransactionType.EXPENSE, r1.getType());
        Assertions.assertEquals("CMP PPI Wallet", r1.getMerchant());

        // Row 3 (Interest): 97.00 Income (deposit)
        ParsedStatementRow rInterest = rows.get(2);
        Assertions.assertEquals(new BigDecimal("97.00"), rInterest.getAmount());
        Assertions.assertEquals(TransactionType.INCOME, rInterest.getType());
        Assertions.assertEquals("Interest Paid", rInterest.getMerchant());

        // Row 4 (PFMS Credit): 1809.00 Income
        ParsedStatementRow rPfms = rows.get(3);
        Assertions.assertEquals(new BigDecimal("1809.00"), rPfms.getAmount());
        Assertions.assertEquals(TransactionType.INCOME, rPfms.getType());

        // Row 5 (NEFT Salary): 31744.00 Income
        ParsedStatementRow rNeft = rows.get(4);
        Assertions.assertEquals(LocalDate.of(2026, 10, 2), rNeft.getDate());
        Assertions.assertEquals(new BigDecimal("31744.00"), rNeft.getAmount());
        Assertions.assertEquals(TransactionType.INCOME, rNeft.getType());
        Assertions.assertTrue(rNeft.getMerchant().contains("RANGAM INFOTECH"));

        // Row 6 (Taco Bell): 114.00 Expense
        ParsedStatementRow rTaco = rows.get(5);
        Assertions.assertEquals(new BigDecimal("114.00"), rTaco.getAmount());
        Assertions.assertEquals(TransactionType.EXPENSE, rTaco.getType());
        Assertions.assertEquals("Taco Bell Ober", rTaco.getMerchant());

        // Row 7 (MB Sent): 430.00 Expense
        ParsedStatementRow rMb = rows.get(6);
        Assertions.assertEquals(new BigDecimal("430.00"), rMb.getAmount());
        Assertions.assertEquals(TransactionType.EXPENSE, rMb.getType());
        Assertions.assertEquals("ATHARV SHANMUKH DHUP", rMb.getMerchant());

        // Row 8 (Reversal / Refund): 375.00 Income
        ParsedStatementRow rRev = rows.get(7);
        Assertions.assertEquals(new BigDecimal("375.00"), rRev.getAmount());
        Assertions.assertEquals(TransactionType.INCOME, rRev.getType());
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
