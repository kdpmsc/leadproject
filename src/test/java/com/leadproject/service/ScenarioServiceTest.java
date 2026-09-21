package com.leadproject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import com.leadproject.dto.LeadCreateRequest;
import com.leadproject.model.Lead;
import com.leadproject.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class ScenarioServiceTest {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ScenarioService scenarioService;

    @Test
    void shouldRunFullLeadScenario() {
        LeadCreateRequest request = new LeadCreateRequest();
        request.setName("Aisha Rahman");
        request.setPhone("+971555123456");
        request.setEmail("aisha@example.com");
        request.setSource("website");
        request.setIndustry("real_estate");
        request.setIntent("buy");
        request.setPropertyType("2_bedroom_apartment");
        request.setBudgetRange("AED 1.5M-2.0M");
        request.setPreferredLocation("Dubai Marina");
        request.setTimeline("within_60_days");
        request.setDecisionMaker("yes");
        request.setPreferredCallbackTime("evening");
        request.setQualificationNotes("Interested in luxury apartment");

        Lead lead = leadRepository.save(new LeadService(leadRepository).createLead(request));

        Map<String, Object> result = scenarioService.runLeadScenario(
                lead.getId(),
                lead.getPhone(),
                lead.getName(),
                "I am looking for a 2-bedroom apartment in Dubai Marina with a budget around AED 2M. I plan to buy within 60 days.",
                "real-estate-playbook",
                7L,
                "Sales follow-up after qualification"
        );

        assertNotNull(result);
        assertEquals("READY_FOR_SALES", result.get("scenarioStatus"));
        assertEquals("QUALIFIED", result.get("leadStatus"));
        assertEquals("HOT", result.get("salesBriefPriority"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        VoiceCallService voiceCallService() {
            return new VoiceCallService() {
                @Override
                public Map<String, Object> placeCall(String toPhone, String leadName, Long leadId) {
                    return Map.of(
                            "leadId", leadId,
                            "leadName", leadName,
                            "toPhone", toPhone,
                            "fromPhone", "+971500000000",
                            "provider", "plivo",
                            "status", "queued"
                    );
                }
            };
        }
    }
}
