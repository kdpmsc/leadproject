package com.leadproject.controller;

import java.util.List;
import java.util.Map;

import com.leadproject.dto.CallTranscriptRequest;
import com.leadproject.model.LeadCall;
import com.leadproject.service.LeadCallService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CallTranscriptController {

    private final LeadCallService leadCallService;

    public CallTranscriptController(LeadCallService leadCallService) {
        this.leadCallService = leadCallService;
    }

    @PostMapping("/calls/{callId}/transcript")
    public ResponseEntity<LeadCall> saveTranscript(
            @PathVariable Long callId,
            @Valid @RequestBody CallTranscriptRequest request) {

        return ResponseEntity.ok(leadCallService.updateCall(
                callId,
                request.getTranscript(),
                request.getRecordingUrl(),
                request.getSummary(),
                request.getStatus()
        ));
    }

    @GetMapping("/calls/lead/{leadId}")
    public ResponseEntity<List<LeadCall>> getLeadCallHistory(@PathVariable Long leadId) {
        return ResponseEntity.ok(leadCallService.getCallHistory(leadId));
    }

    @GetMapping("/calls/summary")
    public ResponseEntity<Map<String, Object>> getCallSummary() {
        return ResponseEntity.ok(Map.of(
                "totalCalls", 12,
                "completedCalls", 9,
                "failedCalls", 2,
                "hotLeads", 5
        ));
    }
}
