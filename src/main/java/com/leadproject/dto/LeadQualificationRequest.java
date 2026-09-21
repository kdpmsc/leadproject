package com.leadproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeadQualificationRequest {

    @NotBlank
    private String transcript;

    @NotBlank
    private String playbook;
}
