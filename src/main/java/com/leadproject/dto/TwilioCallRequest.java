package com.leadproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TwilioCallRequest {

    @NotBlank
    private String phone;

    @NotBlank
    private String leadName;

    @NotNull
    private Long leadId;

    private String type = "iv";
}