package com.leadproject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryResponse {
    private long queued;
    private long calledToday;
    private long qualified;
    private long optedOut;
    private long hotLeads;
    private long complianceAlerts;
    private long callsCompleted;
    private long callsFailed;
}
