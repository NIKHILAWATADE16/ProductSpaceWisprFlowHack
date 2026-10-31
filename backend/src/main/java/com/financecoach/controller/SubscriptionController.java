package com.financecoach.controller;

import com.financecoach.dto.SubscriptionDto;
import com.financecoach.dto.SubscriptionRequest;
import com.financecoach.security.UserPrincipal;
import com.financecoach.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
