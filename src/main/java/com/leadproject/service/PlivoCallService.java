package com.leadproject.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(name = "voice.provider", havingValue = "plivo")
public class PlivoCallService implements VoiceCallService {

    private static final Logger logger = LoggerFactory.getLogger(PlivoCallService.class);

    private final RestTemplate restTemplate;

    @Value("${plivo.auth-id:}")
    private String authId;

    @Value("${plivo.auth-token:}")
    private String authToken;

    @Value("${plivo.phone-number:}")
    private String plivoPhoneNumber;

    @Value("${plivo.app-base-url:http://localhost:8080}")
    private String appBaseUrl;

    public PlivoCallService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    PlivoCallService(RestTemplateBuilder restTemplateBuilder, String authId, String authToken) {
        this.restTemplate = restTemplateBuilder.build();
        this.authId = authId;
        this.authToken = authToken;
    }

    RestTemplate getRestTemplate() {
        return restTemplate;
    }

    @Override
    public Map<String, Object> placeCall(String toPhone, String leadName, Long leadId) {
        logger.info("Starting Plivo call: leadId={}, toPhone={}, leadName={}", leadId, toPhone, leadName);
        if (plivoPhoneNumber == null || plivoPhoneNumber.isBlank()) {
            logger.error("Plivo call rejected: PLIVO_PHONE_NUMBER is not configured");
            throw new IllegalStateException("Plivo phone number is not configured");
        }
        if (authId == null || authId.isBlank() || authToken == null || authToken.isBlank()) {
            logger.error("Plivo call rejected: authentication is not configured");
            throw new IllegalStateException("Plivo authentication is not configured");
        }

        String callbackUrl = appBaseUrl + "/api/v1/voice/inbound?leadId=" + leadId + "&leadName=" + leadName;
        String endpoint = "https://api.plivo.com/v1/Account/" + authId + "/Call/";
        logger.info("Plivo call callback configured: leadId={}, answerUrl={}", leadId, callbackUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(authId, authToken);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("from", plivoPhoneNumber);
        form.add("to", toPhone);
        form.add("answer_url", callbackUrl);
        form.add("answer_method", "GET");
        form.add("ring_timeout", "45");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                URI.create(endpoint),
                HttpMethod.POST,
                request,
                Map.class
            );
        } catch (RuntimeException exception) {
            logger.error("Plivo call creation failed: leadId={}, toPhone={}", leadId, toPhone, exception);
            throw exception;
        }

        Map<String, Object> payload = response.getBody() == null ? Map.of() : response.getBody();
        String callId = String.valueOf(payload.getOrDefault("request_uuid", "unknown"));
        logger.info("Plivo call created: leadId={}, callId={}, responseStatus={}",
            leadId, callId, response.getStatusCode());

        return Map.of(
                "leadId", leadId,
                "leadName", leadName,
                "toPhone", toPhone,
                "fromPhone", plivoPhoneNumber,
                "callId", callId,
                "provider", "plivo",
                "status", "queued",
                "createdAt", LocalDateTime.now().toString()
        );
    }
}
