package com.leadproject.dto;

import java.time.LocalDateTime;

import com.leadproject.model.Lead;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeadResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String industry;
    private String source;
    private String status;
    private Long ownerId;
    private Integer score;
    private String scoreBand;
    private String summary;
    private String leadIntent;
    private String propertyType;
    private String budgetRange;
    private String preferredLocation;
    private String timeline;
    private String decisionMaker;
    private String preferredCallbackTime;
    private String qualificationNotes;
    private String callDisposition;
    private String consentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LeadResponse from(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .name(lead.getName())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .industry(lead.getIndustry())
                .source(lead.getSource())
                .status(lead.getStatus())
                .ownerId(lead.getOwnerId())
                .score(lead.getScore())
                .scoreBand(lead.getScoreBand())
                .summary(lead.getSummary())
                .leadIntent(lead.getLeadIntent())
                .propertyType(lead.getPropertyType())
                .budgetRange(lead.getBudgetRange())
                .preferredLocation(lead.getPreferredLocation())
                .timeline(lead.getTimeline())
                .decisionMaker(lead.getDecisionMaker())
                .preferredCallbackTime(lead.getPreferredCallbackTime())
                .qualificationNotes(lead.getQualificationNotes())
                .callDisposition(lead.getCallDisposition())
                .consentStatus(lead.getConsentStatus().name())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }
}
