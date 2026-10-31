package com.financecoach.controller;

import com.financecoach.dto.CategorySpendingDto;
import com.financecoach.dto.DashboardSummaryDto;
import com.financecoach.security.UserPrincipal;
import com.financecoach.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year
    ) {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary(principal.getId(), month, year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/spending")
    public ResponseEntity<List<CategorySpendingDto>> getSpendingBreakdown(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer year
    ) {
        List<CategorySpendingDto> spending = dashboardService.getCategorySpendingForMonth(principal.getId(), month, year);
        return ResponseEntity.ok(spending);
    }
}

