package com.leadproject.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhoneCallPlan {
    private String phone;
    private String leadName;
    private String source;
    private String industry;
    private String scriptIntro;
    private List<String> questions;
    private String summaryTemplate;
    private String nextAction;
}