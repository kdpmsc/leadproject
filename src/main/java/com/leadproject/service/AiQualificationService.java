package com.leadproject.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiQualificationService {

    private static final Logger logger = LoggerFactory.getLogger(AiQualificationService.class);

    private final ChatClient chatClient;

    public AiQualificationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Map<String, Object> qualifyLead(String transcript, String playbookName) {
        logger.info("Starting AI lead qualification: playbook={}, transcriptLength={}",
            playbookName, transcript == null ? 0 : transcript.length());
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

        String json = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

            logger.info("AI lead qualification completed: playbook={}, responseLength={}",
                playbookName, json == null ? 0 : json.length());
        return Map.of("raw_response", json, "playbook", playbookName);
    }
}
