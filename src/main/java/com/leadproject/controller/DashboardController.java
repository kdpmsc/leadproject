package com.leadproject.controller;

import com.leadproject.dto.DashboardSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    @GetMapping("/dashboard")
    public DashboardSummaryResponse dashboard() {
        return DashboardSummaryResponse.builder()
                .queued(18)
                .calledToday(42)
                .qualified(9)
                .optedOut(3)
                .hotLeads(5)
                .complianceAlerts(1)
                .callsCompleted(9)
                .callsFailed(2)
                .build();
    }
}
