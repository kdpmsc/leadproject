package com.leadproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CallTranscriptRequest {

    @NotBlank
    private String transcript;

    private String recordingUrl;
    private String summary;
    private String status = "COMPLETED";
}