package com.financecoach.controller;

import com.financecoach.dto.DetectedSubscriptionDto;
import com.financecoach.dto.SubscriptionDto;
import com.financecoach.dto.SubscriptionRequest;
import com.financecoach.security.UserPrincipal;
import com.financecoach.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> list(@AuthenticationPrincipal UserPrincipal principal,
                                                       @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(service.list(principal.getId(), activeOnly));
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(principal.getId(), request));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SubscriptionDto> confirm(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(service.setActive(principal.getId(), id, true));
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<SubscriptionDto> dismiss(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(service.setActive(principal.getId(), id, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id) {
        service.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<SubscriptionDto>> getAlerts(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(service.getActiveAlerts(principal.getId()));
    }

    @GetMapping("/detected")
    public ResponseEntity<List<DetectedSubscriptionDto>> getDetected(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(service.detectSubscriptions(principal.getId()));
    }

    @PostMapping("/detect")
    public ResponseEntity<?> detectAndSync(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "false") boolean autoSync) {
        if (autoSync) {
            return ResponseEntity.ok(service.autoSyncDetectedSubscriptions(principal.getId()));
        }
        return ResponseEntity.ok(service.detectSubscriptions(principal.getId()));
    }
}
