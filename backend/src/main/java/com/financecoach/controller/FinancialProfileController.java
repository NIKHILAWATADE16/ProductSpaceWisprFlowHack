package com.financecoach.controller;

import com.financecoach.dto.FinancialProfileDto;
import com.financecoach.dto.ProfileUpdateRequest;
import com.financecoach.security.UserPrincipal;
import com.financecoach.service.FinancialProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class FinancialProfileController {

    private final FinancialProfileService profileService;

    public FinancialProfileController(FinancialProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<FinancialProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        FinancialProfileDto profile = profileService.getProfileByUserId(principal.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<FinancialProfileDto> updateProfile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {
        FinancialProfileDto updated = profileService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(updated);
    }
}

