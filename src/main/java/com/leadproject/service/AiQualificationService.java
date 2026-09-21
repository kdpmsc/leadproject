package com.leadproject.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiQualificationService {

    private final ChatClient chatClient;

    public AiQualificationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Map<String, Object> qualifyLead(String transcript, String playbookName) {
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

        return Map.of("raw_response", json, "playbook", playbookName);
    }
}
