package com.leadproject.controller;

import com.leadproject.dto.LeadQualificationRequest;
import com.leadproject.service.AiQualificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AiController {

    private final AiQualificationService aiQualificationService;

    public AiController(AiQualificationService aiQualificationService) {
        this.aiQualificationService = aiQualificationService;
    }

    @PostMapping("/ai/qualify")
    public Map<String, Object> qualifyLead(@Valid @RequestBody LeadQualificationRequest request) {
        return aiQualificationService.qualifyLead(request.getTranscript(), request.getPlaybook());
    }
}
