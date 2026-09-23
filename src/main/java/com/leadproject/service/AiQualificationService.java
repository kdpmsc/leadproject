package com.leadproject.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiQualificationService {

    private static final Logger logger = LoggerFactory.getLogger(AiQualificationService.class);

    private final ChatClient chatClient;
    private final String ollamaBaseUrl;
    private final String ollamaModel;

    public AiQualificationService(
            ChatClient.Builder chatClientBuilder,
            @Value("${spring.ai.ollama.base-url}") String ollamaBaseUrl,
            @Value("${spring.ai.ollama.chat.options.model}") String ollamaModel) {
        this.chatClient = chatClientBuilder.build();
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.ollamaModel = ollamaModel;
        logger.info("Ollama qualification client configured: chatUrl={}, model={}", chatUrl(), ollamaModel);
    }

    public Map<String, Object> qualifyLead(String transcript, String playbookName) {
        logger.info("Starting Ollama lead qualification: chatUrl={}, model={}, playbook={}, transcriptLength={}",
            chatUrl(), ollamaModel, playbookName, transcript == null ? 0 : transcript.length());
        String prompt = """
                You are a compliant UAE lead qualification assistant.
                Use only approved playbook questions and extract structured facts.
                Playbook: %s
                Transcript:
                %s

                Return JSON with:
                - disposition (qualified|nurture|unqualified|opt_out|callback_requested|unknown)
                - summary
                - recommended_next_action
                - risk_flags
                - fields with intent, budget, timeline, location_or_jurisdiction, decision_maker, preferred_callback_time
                """.formatted(playbookName, transcript);

        String json;
        try {
            json = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            logger.error("Ollama qualification request failed: url={}, model={}", chatUrl(), ollamaModel,
                exception);
            throw exception;
        }

            logger.info("AI lead qualification completed: playbook={}, responseLength={}",
                playbookName, json == null ? 0 : json.length());
        return Map.of("raw_response", json, "playbook", playbookName);
    }

    private String chatUrl() {
        return ollamaBaseUrl.endsWith("/") ? ollamaBaseUrl + "api/chat" : ollamaBaseUrl + "/api/chat";
    }
}
