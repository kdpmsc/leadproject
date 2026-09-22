package com.leadproject.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatRequest {

    @NotBlank
    @Schema(description = "Message sent to the local Ollama model", example = "What information should I collect from a Dubai property buyer?")
    private String message;

    @Schema(description = "Optional previous conversation context", example = "Assistant: Hello. User: I want to buy a property.")
    private String conversation;
}
