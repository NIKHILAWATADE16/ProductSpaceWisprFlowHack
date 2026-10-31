package com.financecoach.controller;

import com.financecoach.service.DemoDataSeeder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

