package com.leadproject.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeadImportResponse {
    private int imported;
    private int skipped;
    private List<String> errors;
}