package com.leadproject.controller;

import java.util.Map;

import com.leadproject.model.Lead;
import com.leadproject.service.LeadService;
import com.leadproject.service.ScenarioService;
import com.leadproject.service.VoiceCallService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CallFlowController {

    private final LeadService leadService;
    private final VoiceCallService voiceCallService;
    private final ScenarioService scenarioService;

    public CallFlowController(LeadService leadService, VoiceCallService voiceCallService, ScenarioService scenarioService) {
        this.leadService = leadService;
        this.voiceCallService = voiceCallService;
        this.scenarioService = scenarioService;
    }

    @PostMapping("/call-flow/start")
    public ResponseEntity<Map<String, Object>> startCallFlow(@Valid @RequestBody Map<String, Object> payload) {
        String phone = String.valueOf(payload.get("phone"));
        String leadName = String.valueOf(payload.get("leadName"));
        Long leadId = resolveLeadId(payload.get("leadId"), phone, leadName);
        return ResponseEntity.ok(voiceCallService.placeCall(phone, leadName, leadId));
    }

    private Long resolveLeadId(Object requestedLeadId, String phone, String leadName) {
        if (requestedLeadId != null && !String.valueOf(requestedLeadId).isBlank()
                && !"null".equalsIgnoreCase(String.valueOf(requestedLeadId))) {
            Long leadId = Long.valueOf(String.valueOf(requestedLeadId));
            if (leadService.findLead(leadId).isPresent()) {
                return leadId;
            }
        }

        Lead lead;
        try {
            lead = leadService.getLeadByPhone(phone);
        } catch (IllegalArgumentException exception) {
            lead = leadService.getOrCreateCallLead(phone, leadName);
        }
        return lead.getId();
    }

    @PostMapping("/call-flow/scenario")
    public ResponseEntity<Map<String, Object>> runScenario(@Valid @RequestBody Map<String, Object> payload) {
        Long leadId = Long.valueOf(String.valueOf(payload.get("leadId")));
        String phone = String.valueOf(payload.get("phone"));
        String leadName = String.valueOf(payload.get("leadName"));
        String transcript = String.valueOf(payload.getOrDefault("transcript", ""));
        String playbookName = String.valueOf(payload.getOrDefault("playbookName", "real-estate-playbook"));
        Long userId = Long.valueOf(String.valueOf(payload.getOrDefault("userId", 0L)));
        String salesSummary = String.valueOf(payload.getOrDefault("salesSummary", "Sales follow-up after qualification"));

        return ResponseEntity.ok(scenarioService.runLeadScenario(
                leadId,
                phone,
                leadName,
                transcript,
                playbookName,
                userId,
                salesSummary
        ));
    }
}
