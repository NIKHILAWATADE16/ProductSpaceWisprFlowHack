package com.financecoach.util;

import com.financecoach.entity.TransactionType;
import com.financecoach.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmsStatementParserTest {

    @Test
    @DisplayName("Should parse various debit SMS banking formats")
    void testParseDebitSmsFormats() {
        String msg1 = "Sent Rs. 450.00 to Swiggy on 15-10-2026. Ref: 981248";
        ParsedStatementRow r1 = SmsStatementParser.parseSmsLine(msg1);
        assertNotNull(r1);
        assertEquals(new BigDecimal("450.00"), r1.getAmount());
        assertEquals(TransactionType.EXPENSE, r1.getType());
        assertEquals(LocalDate.of(2026, 10, 15), r1.getDate());
        assertTrue(r1.getDescription().contains("Swiggy"));

        String msg2 = "A/c *4892 debited for INR 1,200.00 at Grocery Store on 2026-10-14. Avl bal: Rs 45,000";
        ParsedStatementRow r2 = SmsStatementParser.parseSmsLine(msg2);
        assertNotNull(r2);
        assertEquals(new BigDecimal("1200.00"), r2.getAmount());
        assertEquals(TransactionType.EXPENSE, r2.getType());
        assertEquals(LocalDate.of(2026, 10, 14), r2.getDate());
        assertTrue(r2.getDescription().contains("Grocery Store"));

        String msg3 = "Your card ending 1234 was charged $42.50 at Uber Technologies on 12-Oct-26";
        ParsedStatementRow r3 = SmsStatementParser.parseSmsLine(msg3);
        assertNotNull(r3);
        assertEquals(new BigDecimal("42.50"), r3.getAmount());
        assertEquals(TransactionType.EXPENSE, r3.getType());
        assertEquals(LocalDate.of(2026, 10, 12), r3.getDate());
        assertTrue(r3.getDescription().contains("Uber"));

        String msg4 = "Paid Rs 250.00 to Starbucks Coffee via UPI on 18/10/2026";
        ParsedStatementRow r4 = SmsStatementParser.parseSmsLine(msg4);
        assertNotNull(r4);
        assertEquals(new BigDecimal("250.00"), r4.getAmount());
        assertEquals(TransactionType.EXPENSE, r4.getType());
        assertEquals(LocalDate.of(2026, 10, 18), r4.getDate());
        assertTrue(r4.getDescription().contains("Starbucks"));
    }

    @Test
    @DisplayName("Should parse credit and salary SMS messages")
    void testParseCreditSmsFormats() {
        String msg1 = "Salary credited: INR 65,000.00 from Tech Corp on 2026-10-01. Balance Rs 95,000";
        ParsedStatementRow r1 = SmsStatementParser.parseSmsLine(msg1);
        assertNotNull(r1);
        assertEquals(new BigDecimal("65000.00"), r1.getAmount());
        assertEquals(TransactionType.INCOME, r1.getType());
        assertEquals(LocalDate.of(2026, 10, 1), r1.getDate());
        assertTrue(r1.getDescription().contains("Tech Corp"));

        String msg2 = "Account *1234 credited with $500.00 from ACME Inc on 2026-10-05";
        ParsedStatementRow r2 = SmsStatementParser.parseSmsLine(msg2);
        assertNotNull(r2);
        assertEquals(new BigDecimal("500.00"), r2.getAmount());
        assertEquals(TransactionType.INCOME, r2.getType());
        assertEquals(LocalDate.of(2026, 10, 5), r2.getDate());
        assertTrue(r2.getDescription().contains("ACME"));
    }

    @Test
    @DisplayName("Should parse multi-message text block")
    void testParseMultiMessageTextBlock() {
        String block = """
                Sent Rs. 450.00 to Swiggy on 15-10-2026. Ref: 981248
                
                A/c *4892 debited for INR 1,200.00 at Grocery Store on 2026-10-14. Avl bal: Rs 45,000
                
                Salary credited: INR 65,000.00 from Tech Corp on 2026-10-01. Balance Rs 95,000
                """;

        List<ParsedStatementRow> rows = SmsStatementParser.parseText(block);
        assertEquals(3, rows.size());
        assertEquals(TransactionType.EXPENSE, rows.get(0).getType());
        assertEquals(TransactionType.EXPENSE, rows.get(1).getType());
        assertEquals(TransactionType.INCOME, rows.get(2).getType());
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid or empty text")
    void testEmptyOrInvalidSms() {
        assertThrows(BadRequestException.class, () -> SmsStatementParser.parseText(""));
        assertThrows(BadRequestException.class, () -> SmsStatementParser.parseText("Hello, this is just a normal chat message without money"));
    }
}
