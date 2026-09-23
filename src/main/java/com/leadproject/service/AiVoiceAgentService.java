package com.leadproject.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiVoiceAgentService {

    private static final Logger logger = LoggerFactory.getLogger(AiVoiceAgentService.class);

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
    private final String ollamaBaseUrl;
    private final String ollamaModel;

    public AiVoiceAgentService(
            ChatClient.Builder chatClientBuilder,
            @Value("${spring.ai.ollama.base-url}") String ollamaBaseUrl,
            @Value("${spring.ai.ollama.chat.options.model}") String ollamaModel) {
        this.chatClient = chatClientBuilder.build();
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.ollamaModel = ollamaModel;
        logger.info("Ollama AI client configured: chatUrl={}, model={}", chatUrl(), ollamaModel);
    }

    public String respond(String leadName, String conversation) {
        logger.info("Calling Ollama voice agent: url={}, model={}, conversationLength={}",
            chatUrl(), ollamaModel, conversation == null ? 0 : conversation.length());
        String prompt = """
                Lead name: %s
                Conversation so far:
                %s

                Continue the conversation. If this is the beginning, greet the lead and
                ask whether they want to buy, rent, sell, or invest.
                """.formatted(leadName == null || leadName.isBlank() ? "there" : leadName,
                conversation == null || conversation.isBlank() ? "(no messages yet)" : conversation);

        String response;
        try {
            response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();
        } catch (RuntimeException exception) {
            logger.error("Ollama voice-agent request failed: url={}, model={}", chatUrl(), ollamaModel,
                exception);
            throw exception;
        }

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI model returned an empty voice-agent response");
        }
        return response.trim();
    }

    public String chat(String message, String conversation) {
        logger.info("Calling Ollama chat API: url={}, model={}, messageLength={}, conversationLength={}",
            chatUrl(), ollamaModel, message == null ? 0 : message.length(),
            conversation == null ? 0 : conversation.length());
        String prompt = """
                Conversation so far:
                %s

                User message:
                %s

                Reply naturally and helpfully. Keep the response concise.
                """.formatted(
                conversation == null || conversation.isBlank() ? "(new conversation)" : conversation,
                message);

        String response;
        try {
            response = chatClient.prompt()
                    .system("You are a helpful local AI assistant for development testing.")
                    .user(prompt)
                    .call()
                    .content();
        } catch (RuntimeException exception) {
            logger.error("Ollama chat request failed: url={}, model={}", chatUrl(), ollamaModel, exception);
            throw exception;
        }

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI model returned an empty chat response");
        }
        return response.trim();
    }

    private String chatUrl() {
        return ollamaBaseUrl.endsWith("/") ? ollamaBaseUrl + "api/chat" : ollamaBaseUrl + "/api/chat";
    }
}
