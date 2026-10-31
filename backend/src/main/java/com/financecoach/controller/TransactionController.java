package com.financecoach.controller;

import com.financecoach.dto.CategorizationRuleDto;
import com.financecoach.dto.SmsImportRequest;
import com.financecoach.dto.TransactionDto;
import com.financecoach.dto.TransactionRequest;
import com.financecoach.entity.TransactionType;
import com.financecoach.security.UserPrincipal;
import com.financecoach.service.CategorizationService;
import com.financecoach.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;
    private final CategorizationService categorizationService;

    public TransactionController(TransactionService service, CategorizationService categorizationService) {
        this.service = service;
        this.categorizationService = categorizationService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> list(
        @AuthenticationPrincipal UserPrincipal p,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) TransactionType type,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(service.list(p.getId(), query, type, category, from, to));
    }

    @PostMapping
    public ResponseEntity<TransactionDto> create(
        @AuthenticationPrincipal UserPrincipal p,
        @Valid @RequestBody TransactionRequest r
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p.getId(), r));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> get(
        @AuthenticationPrincipal UserPrincipal p,
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.get(p.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> update(
        @AuthenticationPrincipal UserPrincipal p,
        @PathVariable Long id,
        @Valid @RequestBody TransactionRequest r
    ) {
        return ResponseEntity.ok(service.update(p.getId(), id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal UserPrincipal p,
        @PathVariable Long id
    ) {
        service.delete(p.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importFile(
        @AuthenticationPrincipal UserPrincipal p,
        @RequestPart("file") MultipartFile file
    ) {
        Map<String, Object> result = service.importStatement(p.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/import-sms")
    public ResponseEntity<Map<String, Object>> importSms(
        @AuthenticationPrincipal UserPrincipal p,
        @Valid @RequestBody SmsImportRequest request
    ) {
        Map<String, Object> result = service.importSms(p.getId(), request.getSmsText());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(categorizationService.getAllCategories());
    }

    @GetMapping("/rules")
    public ResponseEntity<List<CategorizationRuleDto>> getRules(@AuthenticationPrincipal UserPrincipal p) {
        return ResponseEntity.ok(categorizationService.getUserRules(p.getId()));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@AuthenticationPrincipal UserPrincipal p, @PathVariable Long id) {
        categorizationService.deleteRule(p.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
