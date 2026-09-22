package com.leadproject.controller;

import com.leadproject.dto.LeadQualificationRequest;
import com.leadproject.dto.AiChatRequest;
import com.leadproject.service.AiQualificationService;
import com.leadproject.service.AiVoiceAgentService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AiController {

    private final AiQualificationService aiQualificationService;
    private final AiVoiceAgentService aiVoiceAgentService;

    public AiController(AiQualificationService aiQualificationService, AiVoiceAgentService aiVoiceAgentService) {
        this.aiQualificationService = aiQualificationService;
        this.aiVoiceAgentService = aiVoiceAgentService;
    }

    @PostMapping("/ai/qualify")
    public Map<String, Object> qualifyLead(@Valid @RequestBody LeadQualificationRequest request) {
        return aiQualificationService.qualifyLead(request.getTranscript(), request.getPlaybook());
    }

    @PostMapping("/ai/chat")
    @Operation(summary = "Chat with the local Ollama model",
            description = "Sends a development chat message to the configured local Ollama model without starting a Twilio call.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI reply generated"),
            @ApiResponse(responseCode = "400", description = "Message is missing", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ollama is unavailable or the configured model is missing", content = @Content)
    })
    public Map<String, String> chat(@Valid @RequestBody AiChatRequest request) {
        return Map.of("reply", aiVoiceAgentService.chat(request.getMessage(), request.getConversation()));
    }
}
