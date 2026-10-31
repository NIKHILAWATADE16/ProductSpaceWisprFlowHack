package com.financecoach.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfStatementGenerator {

    public static record StatementLine(String date, String description, String amount, String type) {
        public String toFormattedRow() {
            return String.format("%s %s %s %s", date, description, amount, type);
        }
    }

    public static List<StatementLine> getSample6MonthExpenses() {
        List<StatementLine> lines = new ArrayList<>();

        // Month 1: May 2026
        lines.add(new StatementLine("2026-05-01", "Tech Corp Salary Direct Deposit", "6500.00", "CR"));
        lines.add(new StatementLine("2026-05-02", "Maple Apartments Monthly Rent", "1800.00", "DR"));
        lines.add(new StatementLine("2026-05-03", "Whole Foods Market Grocery", "142.50", "DR"));
        lines.add(new StatementLine("2026-05-05", "Starbucks Coffee #421", "14.80", "DR"));
        lines.add(new StatementLine("2026-05-07", "Netflix.com Subscription", "15.49", "DR"));
        lines.add(new StatementLine("2026-05-09", "Pacific Gas & Electric Utility", "118.20", "DR"));
        lines.add(new StatementLine("2026-05-11", "Chipotle Mexican Grill", "22.40", "DR"));
        lines.add(new StatementLine("2026-05-13", "Spotify USA Monthly", "10.99", "DR"));
        lines.add(new StatementLine("2026-05-15", "Shell Oil Gas Station", "52.30", "DR"));
        lines.add(new StatementLine("2026-05-17", "Planet Fitness Club Gym", "45.00", "DR"));
        lines.add(new StatementLine("2026-05-20", "Trader Joe's Supermarket", "128.60", "DR"));
        lines.add(new StatementLine("2026-05-24", "Amazon.com*Purchase", "74.90", "DR"));
        lines.add(new StatementLine("2026-05-28", "Italian Osteria Dinner", "110.00", "DR"));

        // Month 2: June 2026
        lines.add(new StatementLine("2026-06-01", "Tech Corp Salary Direct Deposit", "6500.00", "CR"));
        lines.add(new StatementLine("2026-06-02", "Maple Apartments Monthly Rent", "1800.00", "DR"));
        lines.add(new StatementLine("2026-06-04", "Trader Joe's Supermarket", "135.20", "DR"));
        lines.add(new StatementLine("2026-06-06", "Blue Bottle Coffee Downtown", "16.50", "DR"));
        lines.add(new StatementLine("2026-06-07", "Netflix.com Subscription", "15.49", "DR"));
        lines.add(new StatementLine("2026-06-10", "Comcast Xfinity Internet", "79.99", "DR"));
        lines.add(new StatementLine("2026-06-12", "Sweetgreen Salads Downtown", "28.50", "DR"));
        lines.add(new StatementLine("2026-06-13", "Spotify USA Monthly", "10.99", "DR"));
        lines.add(new StatementLine("2026-06-15", "Chevron Fuel Station", "58.40", "DR"));
        lines.add(new StatementLine("2026-06-17", "Planet Fitness Club Gym", "45.00", "DR"));
        lines.add(new StatementLine("2026-06-20", "Whole Foods Market Grocery", "164.80", "DR"));
        lines.add(new StatementLine("2026-06-23", "AWS Cloud Services", "28.50", "DR"));
        lines.add(new StatementLine("2026-06-26", "Target Store #1842", "86.40", "DR"));

        // Month 3: July 2026
        lines.add(new StatementLine("2026-07-01", "Tech Corp Salary Direct Deposit", "6500.00", "CR"));
        lines.add(new StatementLine("2026-07-02", "Maple Apartments Monthly Rent", "1800.00", "DR"));
        lines.add(new StatementLine("2026-07-03", "Safeway Grocery Store", "148.90", "DR"));
        lines.add(new StatementLine("2026-07-05", "Starbucks Coffee #421", "15.20", "DR"));
        lines.add(new StatementLine("2026-07-07", "Netflix.com Subscription", "15.49", "DR"));
        lines.add(new StatementLine("2026-07-09", "Pacific Gas & Electric Utility", "132.40", "DR"));
        lines.add(new StatementLine("2026-07-11", "In-N-Out Burger Quick Lunch", "18.90", "DR"));
        lines.add(new StatementLine("2026-07-13", "Spotify USA Monthly", "10.99", "DR"));
        lines.add(new StatementLine("2026-07-16", "Shell Oil Gas Station", "51.80", "DR"));
        lines.add(new StatementLine("2026-07-17", "Planet Fitness Club Gym", "45.00", "DR"));
        lines.add(new StatementLine("2026-07-21", "Trader Joe's Supermarket", "142.10", "DR"));
        lines.add(new StatementLine("2026-07-24", "Nike Sports Store Running Gear", "124.00", "DR"));
        lines.add(new StatementLine("2026-07-27", "Chipotle Mexican Grill", "24.50", "DR"));

        // Month 4: August 2026
        lines.add(new StatementLine("2026-08-01", "Tech Corp Salary Direct Deposit", "6500.00", "CR"));
        lines.add(new StatementLine("2026-08-02", "Maple Apartments Monthly Rent", "1800.00", "DR"));
        lines.add(new StatementLine("2026-08-04", "Whole Foods Market Grocery", "156.40", "DR"));
        lines.add(new StatementLine("2026-08-06", "Blue Bottle Coffee Downtown", "14.50", "DR"));
        lines.add(new StatementLine("2026-08-07", "Netflix.com Subscription", "15.49", "DR"));
        lines.add(new StatementLine("2026-08-10", "Comcast Xfinity Internet", "79.99", "DR"));
        lines.add(new StatementLine("2026-08-12", "Sweetgreen Salads Downtown", "32.10", "DR"));
        lines.add(new StatementLine("2026-08-13", "Spotify USA Monthly", "10.99", "DR"));
        lines.add(new StatementLine("2026-08-15", "Uber Airport Trip Ride", "34.50", "DR"));
        lines.add(new StatementLine("2026-08-17", "Planet Fitness Club Gym", "45.00", "DR"));
        lines.add(new StatementLine("2026-08-20", "Trader Joe's Supermarket", "138.90", "DR"));
        lines.add(new StatementLine("2026-08-23", "AWS Cloud Services", "28.50", "DR"));
        lines.add(new StatementLine("2026-08-26", "Amazon.com*Purchase Order", "94.20", "DR"));
        lines.add(new StatementLine("2026-08-29", "Italian Osteria Dinner", "118.00", "DR"));

        // Month 5: September 2026
        lines.add(new StatementLine("2026-09-01", "Tech Corp Salary Direct Deposit", "6500.00", "CR"));
        lines.add(new StatementLine("2026-09-02", "Maple Apartments Monthly Rent", "1800.00", "DR"));
        lines.add(new StatementLine("2026-09-03", "Safeway Grocery Store", "152.40", "DR"));
        lines.add(new StatementLine("2026-09-05", "Starbucks Coffee #421", "16.10", "DR"));
        lines.add(new StatementLine("2026-09-07", "Netflix.com Subscription", "15.49", "DR"));
        lines.add(new StatementLine("2026-09-09", "Pacific Gas & Electric Utility", "126.80", "DR"));
        lines.add(new StatementLine("2026-09-11", "Chipotle Mexican Grill", "23.50", "DR"));
        lines.add(new StatementLine("2026-09-13", "Spotify USA Monthly", "10.99", "DR"));
        lines.add(new StatementLine("2026-09-16", "Shell Oil Gas Station", "56.20", "DR"));
        lines.add(new StatementLine("2026-09-17", "Planet Fitness Club Gym", "45.00", "DR"));
        lines.add(new StatementLine("2026-09-21", "Whole Foods Market Grocery", "162.70", "DR"));
        lines.add(new StatementLine("2026-09-24", "Best Buy Electronics Purchase", "149.99", "DR"));
        lines.add(new StatementLine("2026-09-28", "CVS Pharmacy Prescription", "38.60", "DR"));

        // Month 6: October 2026
        lines.add(new StatementLine("2026-10-01", "Tech Corp Salary Direct Deposit", "6500.00", "CR"));
        lines.add(new StatementLine("2026-10-02", "Maple Apartments Monthly Rent", "1800.00", "DR"));
        lines.add(new StatementLine("2026-10-04", "Trader Joe's Supermarket", "144.30", "DR"));
        lines.add(new StatementLine("2026-10-06", "Blue Bottle Coffee Downtown", "15.80", "DR"));
        lines.add(new StatementLine("2026-10-07", "Netflix.com Subscription", "15.49", "DR"));
        lines.add(new StatementLine("2026-10-10", "Comcast Xfinity Internet", "79.99", "DR"));
        lines.add(new StatementLine("2026-10-12", "Sweetgreen Salads Downtown", "29.40", "DR"));
        lines.add(new StatementLine("2026-10-13", "Spotify USA Monthly", "10.99", "DR"));
        lines.add(new StatementLine("2026-10-15", "Chevron Fuel Station", "55.10", "DR"));
        lines.add(new StatementLine("2026-10-17", "Planet Fitness Club Gym", "45.00", "DR"));
        lines.add(new StatementLine("2026-10-21", "Whole Foods Market Grocery", "158.20", "DR"));
        lines.add(new StatementLine("2026-10-23", "AWS Cloud Services", "28.50", "DR"));
        lines.add(new StatementLine("2026-10-25", "Amazon.com*Purchase Order", "88.50", "DR"));
        lines.add(new StatementLine("2026-10-29", "Italian Osteria Dinner", "125.00", "DR"));

        return lines;
    }

    public static byte[] generateSample6MonthStatement() throws IOException {
        List<StatementLine> allLines = getSample6MonthExpenses();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PDDocument doc = new PDDocument()) {
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontCourier = new PDType1Font(Standard14Fonts.FontName.COURIER);

            int itemsPerPage = 40;
            int totalPages = (int) Math.ceil((double) allLines.size() / itemsPerPage);

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                int start = pageIndex * itemsPerPage;
                int end = Math.min(start + itemsPerPage, allLines.size());
                List<StatementLine> pageLines = allLines.subList(start, end);

                try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                    float y = 800;

                    // Header
                    stream.beginText();
                    stream.setFont(fontBold, 16);
                    stream.newLineAtOffset(50, y);
                    stream.showText("APEX FIRST NATIONAL BANK");
                    stream.endText();

                    y -= 20;
                    stream.beginText();
                    stream.setFont(fontRegular, 10);
                    stream.newLineAtOffset(50, y);
                    stream.showText("Account Statement  |  Alex Morgan  |  Account # **** **** 4892");
                    stream.endText();

                    y -= 14;
                    stream.beginText();
                    stream.setFont(fontRegular, 9);
                    stream.newLineAtOffset(50, y);
                    stream.showText("Statement Period: 2026-05-01 to 2026-10-31  |  Currency: USD");
                    stream.endText();

                    y -= 25;
                    // Section Divider
                    stream.setLineWidth(1f);
                    stream.moveTo(50, y);
                    stream.lineTo(545, y);
                    stream.stroke();

                    y -= 18;
                    // Column Titles
                    stream.beginText();
                    stream.setFont(fontBold, 9);
                    stream.newLineAtOffset(50, y);
                    stream.showText(String.format("%-12s  %-36s  %10s  %4s", "DATE", "DESCRIPTION", "AMOUNT", "TYPE"));
                    stream.endText();

                    y -= 8;
                    stream.setLineWidth(0.5f);
                    stream.moveTo(50, y);
                    stream.lineTo(545, y);
                    stream.stroke();

                    y -= 14;

                    // Rows
                    stream.setFont(fontCourier, 9);
                    for (StatementLine item : pageLines) {
                        stream.beginText();
                        stream.newLineAtOffset(50, y);
                        // Format: YYYY-MM-DD <Description> <Amount> <Type>
                        // Exactly matches LINE_PATTERN in PdfStatementParser
                        String rowText = String.format("%-10s  %-38s  %8s  %2s",
                                item.date(),
                                item.description().length() > 38 ? item.description().substring(0, 38) : item.description(),
                                item.amount(),
                                item.type());
                        stream.showText(rowText);
                        stream.endText();
                        y -= 14;
                    }

                    // Footer
                    stream.beginText();
                    stream.setFont(fontRegular, 8);
                    stream.newLineAtOffset(50, 30);
                    stream.showText(String.format("Page %d of %d  |  Apex First National Bank Member FDIC", pageIndex + 1, totalPages));
                    stream.endText();
                }
            }

            doc.save(out);
        }

        return out.toByteArray();
    }

    public static void generateAndSave(String filePath) throws IOException {
        byte[] pdfBytes = generateSample6MonthStatement();
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfBytes);
        }
    }

    public static void main(String[] args) throws IOException {
        String path = "sample_6_months_expenses_statement.pdf";
        if (args.length > 0) {
            path = args[0];
        }
        generateAndSave(path);
        System.out.println("Generated sample statement PDF at: " + path);
    }
}
