package com.leadproject.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiVoiceAgentService {

    private static final String SYSTEM_PROMPT = """
            You are a concise UAE real-estate lead qualification voice assistant.
            Have a natural conversation and ask one question at a time.
            Collect the caller's intent, property type, location, budget, timeline,
            decision-maker status, and preferred callback time.
            Acknowledge answers briefly before asking the next useful question.
            Respect requests to stop, opt out, or speak to a human immediately.
            Never invent property availability, prices, legal advice, or guarantees.
            Reply with only the words that should be spoken aloud. Do not use markup,
            labels, emojis, or stage directions.
            """;

    private final ChatClient chatClient;

    public AiVoiceAgentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String respond(String leadName, String conversation) {
        String prompt = """
                Lead name: %s
                Conversation so far:
                %s

                Continue the conversation. If this is the beginning, greet the lead and
                ask whether they want to buy, rent, sell, or invest.
                """.formatted(leadName == null || leadName.isBlank() ? "there" : leadName,
                conversation == null || conversation.isBlank() ? "(no messages yet)" : conversation);

        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI model returned an empty voice-agent response");
        }
        return response.trim();
    }

    public String chat(String message, String conversation) {
        String prompt = """
                Conversation so far:
                %s

                User message:
                %s

                Reply naturally and helpfully. Keep the response concise.
                """.formatted(
                conversation == null || conversation.isBlank() ? "(new conversation)" : conversation,
                message);

        String response = chatClient.prompt()
                .system("You are a helpful local AI assistant for development testing.")
                .user(prompt)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI model returned an empty chat response");
        }
        return response.trim();
    }
}
