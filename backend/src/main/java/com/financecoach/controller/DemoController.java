package com.financecoach.controller;

import com.financecoach.service.DemoDataSeeder;
import com.financecoach.util.PdfStatementGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoDataSeeder seeder;

    public DemoController(DemoDataSeeder seeder) {
        this.seeder = seeder;
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed(
            @RequestParam(defaultValue = "demo@financecoach.com") String email,
            @RequestParam(defaultValue = "Password123!") String password,
            @RequestParam(defaultValue = "Alex Morgan") String fullName,
            @RequestParam(defaultValue = "true") boolean reset) {
        Map<String, Object> response = seeder.seedDemoData(email, password, fullName, reset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
                "email", "demo@financecoach.com",
                "password", "Password123!",
                "fullName", "Alex Morgan",
                "role", "Demo User with 3 Months of Transactions & Active Goals"
        ));
    }

    @GetMapping(value = "/statement.pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> downloadSamplePdf() throws IOException {
        byte[] pdfBytes = PdfStatementGenerator.generateSample6MonthStatement();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample_6_months_expenses_statement.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}

