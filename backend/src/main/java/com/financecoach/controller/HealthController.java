package com.financecoach.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "Personal Finance Coach API");

        try {
            Integer dbResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("database", dbResult != null && dbResult == 1 ? "CONNECTED" : "UNKNOWN");
        } catch (Exception e) {
            response.put("database", "DISCONNECTED: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}

