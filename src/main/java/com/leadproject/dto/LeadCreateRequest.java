package com.leadproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeadCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @Email
    private String email;

    @NotBlank
    private String industry;

    @NotBlank
    private String source;

    private String intent;
    private String propertyType;
    private String budgetRange;
    private String preferredLocation;
    private String timeline;
    private String decisionMaker;
    private String preferredCallbackTime;
    private String qualificationNotes;

    @NotNull
    private ConsentRequest consent;

    @Data
    public static class ConsentRequest {
        private String lawfulBasis = "consent";
        private String capturedAt;
        private String evidenceReference;
    }
}
