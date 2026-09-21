package com.leadproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CallScriptRequest {

    @NotBlank
    private String phone;

    @NotBlank
    private String leadName;

    private String source = "website_form";
    private String industry = "REAL_ESTATE";
    private String callType = "OUTBOUND";
}