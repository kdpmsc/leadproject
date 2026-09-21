package com.leadproject.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.leadproject.dto.SalesBriefRequest;
import com.leadproject.model.Lead;
import com.leadproject.model.LeadCall;
import com.leadproject.repository.LeadCallRepository;
import com.leadproject.repository.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScenarioService {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioService.class);

    private final LeadRepository leadRepository;
    private final LeadCallRepository leadCallRepository;
    private final LeadCallService leadCallService;
    private final SalesBriefService salesBriefService;
    private final VoiceCallService voiceCallService;

    public ScenarioService(
            LeadRepository leadRepository,
            LeadCallRepository leadCallRepository,
            LeadCallService leadCallService,
            SalesBriefService salesBriefService,
            VoiceCallService voiceCallService) {
        this.leadRepository = leadRepository;
        this.leadCallRepository = leadCallRepository;
        this.leadCallService = leadCallService;
        this.salesBriefService = salesBriefService;
        this.voiceCallService = voiceCallService;
    }

    @Transactional
    public Map<String, Object> runLeadScenario(Long leadId, String phone, String leadName, String transcript,
                                              String playbookName, Long userId, String salesSummary) {
        logger.info("Starting lead scenario: leadId={}, phone={}, playbook={}, userId={}",
            leadId, phone, playbookName, userId);
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        Map<String, Object> result = new LinkedHashMap<>();

        lead.setStatus("CALL_PENDING");
        lead.setUpdatedAt(LocalDateTime.now());
        leadRepository.save(lead);

        Map<String, Object> callResponse = voiceCallService.placeCall(phone, leadName, leadId);
        LeadCall call = leadCallService.createCall(leadId, phone, leadName);
        call.setStatus(String.valueOf(callResponse.getOrDefault("status", "QUEUED")));
        call.setUpdatedAt(LocalDateTime.now());
        leadCallRepository.save(call);

        String normalizedTranscript = transcript == null ? "" : transcript;
        String leadIntent = "buy";
        String propertyType = "2-bedroom apartment";
        String budgetRange = "AED 1.5M-2.0M";
        String preferredLocation = "Dubai Marina";
        String timeline = "within 60 days";
        String decisionMaker = "Yes";
        String preferredCallbackTime = "evening";

        LeadService leadService = new LeadService(leadRepository);
        leadService.qualifyLead(
                leadId,
                leadIntent,
                propertyType,
                budgetRange,
                preferredLocation,
                timeline,
                decisionMaker,
                preferredCallbackTime,
                "Qualified through automated scenario flow"
        );

        SalesBriefRequest briefRequest = new SalesBriefRequest();
        briefRequest.setTranscript(normalizedTranscript);
        briefRequest.setSummary(salesSummary);
        Map<String, Object> salesBrief = salesBriefService.buildSalesBrief(leadId, briefRequest);

        lead.setStatus("QUALIFIED");
        lead.setSummary("Scenario run completed; sales brief generated");
        lead.setUpdatedAt(LocalDateTime.now());
        leadRepository.save(lead);

        leadService.assignLead(leadId, userId, "Scenario assignment triggered after qualification");

        result.put("leadId", leadId);
        result.put("leadName", leadName);
        result.put("callId", call.getId());
        result.put("provider", callResponse.getOrDefault("provider", "plivo"));
        result.put("callStatus", callResponse.getOrDefault("status", "queued"));
        result.put("scenarioStatus", "READY_FOR_SALES");
        result.put("leadStatus", lead.getStatus());
        result.put("salesBriefPriority", salesBrief.get("priority"));
        result.put("salesBrief", salesBrief);
        result.put("updatedAt", LocalDateTime.now().toString());
        logger.info("Lead scenario completed: leadId={}, scenarioStatus={}, leadStatus={}",
            leadId, result.get("scenarioStatus"), result.get("leadStatus"));
        return result;
    }
}
