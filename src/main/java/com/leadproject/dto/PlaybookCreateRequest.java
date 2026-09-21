package com.leadproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaybookCreateRequest {

    @NotBlank
    private String industry;

    @NotBlank
    private String name;

    private String status = "DRAFT";
    private String version = "v1";
    private String configurationJson;
    private String approvalState = "DRAFT";
}
