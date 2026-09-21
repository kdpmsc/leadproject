package com.leadproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuppressionCreateRequest {

    @NotBlank
    private String phone;

    @NotBlank
    private String reason;

    private String scope = "ALL_CAMPAIGNS";
    private String source = "manual";
}
