package com.leadproject.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.leadproject.dto.CallScriptRequest;
import com.leadproject.dto.LeadCreateRequest;
import com.leadproject.dto.LeadImportResponse;
import com.leadproject.dto.LeadResponse;
import com.leadproject.dto.PhoneCallPlan;
import com.leadproject.dto.TwilioCallRequest;
import com.leadproject.model.Lead;
import com.leadproject.service.LeadService;
import com.leadproject.service.LeadImportService;
import com.leadproject.service.VoiceCallService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class LeadController {

    private final LeadService leadService;
    private final LeadImportService leadImportService;
    private final VoiceCallService voiceCallService;

    public LeadController(LeadService leadService, LeadImportService leadImportService, VoiceCallService voiceCallService) {
        this.leadService = leadService;
        this.leadImportService = leadImportService;
        this.voiceCallService = voiceCallService;
    }

    @PostMapping("/leads")
    public ResponseEntity<LeadResponse> createLead(@Valid @RequestBody LeadCreateRequest request) {
        Lead lead = leadService.createLead(request);
        return ResponseEntity.ok(LeadResponse.from(lead));
    }

    @GetMapping("/leads")
    public ResponseEntity<List<LeadResponse>> listLeads(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String scoreBand) {
        return ResponseEntity.ok(leadService.listLeads(status, scoreBand)
                .stream()
                .map(LeadResponse::from)
                .toList());
    }

    @GetMapping("/leads/{leadId}")
    public ResponseEntity<LeadResponse> getLead(@PathVariable Long leadId) {
        return ResponseEntity.ok(LeadResponse.from(leadService.getLead(leadId)));
    }

    @PostMapping("/leads/{leadId}/assign")
    public ResponseEntity<Map<String, Object>> assignLead(
            @PathVariable Long leadId,
            @RequestBody Map<String, String> payload) {
        Long userId = Long.parseLong(payload.getOrDefault("user_id", "0"));
        String reason = payload.getOrDefault("reason", "manual_assignment");
        leadService.assignLead(leadId, userId, reason);

        return ResponseEntity.ok(Map.of(
                "lead_id", leadId,
                "assigned_to", userId,
                "reason", reason,
                "updated_at", LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/leads/{leadId}/qualify")
    public ResponseEntity<LeadResponse> qualifyLead(
            @PathVariable Long leadId,
            @RequestBody Map<String, String> payload) {

        String intent = payload.get("intent");
        String propertyType = payload.get("property_type");
        String budgetRange = payload.get("budget_range");
        String preferredLocation = payload.get("preferred_location");
        String timeline = payload.get("timeline");
        String decisionMaker = payload.get("decision_maker");
        String preferredCallbackTime = payload.get("preferred_callback_time");
        String qualificationNotes = payload.get("qualification_notes");

        Lead lead = leadService.qualifyLead(
                leadId,
                intent,
                propertyType,
                budgetRange,
                preferredLocation,
                timeline,
                decisionMaker,
                preferredCallbackTime,
                qualificationNotes
        );

        return ResponseEntity.ok(LeadResponse.from(lead));
    }

    @PostMapping(value = "/leads/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LeadImportResponse> importLeads(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(leadImportService.importFromExcel(file));
    }

    @PostMapping("/leads/call-plan")
    public ResponseEntity<PhoneCallPlan> createCallPlan(@Valid @RequestBody CallScriptRequest request) {
        return ResponseEntity.ok(leadImportService.buildPhoneCallPlan(
                request.getPhone(),
                request.getLeadName(),
                request.getSource()
        ));
    }

    @GetMapping("/leads/phone/{phone}")
    public ResponseEntity<LeadResponse> getLeadByPhone(@PathVariable String phone) {
        String normalizedPhone = leadService.normalizePhone(phone);
        return leadService.findByPhone(normalizedPhone)
                .map(lead -> ResponseEntity.ok(LeadResponse.from(lead)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/leads/call")
    public ResponseEntity<Map<String, Object>> callLead(@Valid @RequestBody TwilioCallRequest request) {
        return ResponseEntity.ok(voiceCallService.placeCall(request.getPhone(), request.getLeadName(), request.getLeadId()));
    }
}
