package com.leadproject.service;

import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallCreator;
import com.twilio.type.PhoneNumber;
import com.leadproject.model.LeadCall;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "voice.provider", havingValue = "twilio", matchIfMissing = true)
public class TwilioCallService implements VoiceCallService {

    @Value("${twilio.phone-number:+17372508034}")
    private String twilioPhoneNumber;

    @Value("${twilio.app-base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${twilio.record-call:false}")
    private boolean recordCall;

    private final LeadCallService leadCallService;

    public TwilioCallService(LeadCallService leadCallService) {
        this.leadCallService = leadCallService;
    }

    @Override
    public Map<String, Object> placeCall(String toPhone, String leadName, Long leadId) {
        if (twilioPhoneNumber == null || twilioPhoneNumber.isBlank()) {
            throw new IllegalStateException("Twilio phone number is not configured");
        }

        LeadCall leadCall = leadCallService.createCall(leadId, toPhone, leadName);
        String callbackUrl = appBaseUrl + "/api/v1/voice/property-qualification?leadId=" + leadId + "&leadName=" + encode(leadName);
        String statusCallbackUrl = appBaseUrl + "/api/v1/voice/status";
        String recordingCallbackUrl = appBaseUrl + "/api/v1/voice/recording";

        CallCreator callCreator = Call.creator(
                new PhoneNumber(toPhone),
                new PhoneNumber(twilioPhoneNumber),
                buildTwimlUri(callbackUrl)
        ).setStatusCallback(statusCallbackUrl);

        if (recordCall) {
            callCreator.setRecord(true).setRecordingStatusCallback(recordingCallbackUrl);
        }

        Call call = callCreator.create();

        leadCallService.attachProviderCall(leadCall.getId(), call.getSid());

        return Map.of(
                "leadId", leadId,
                "leadName", leadName,
                "toPhone", toPhone,
                "fromPhone", twilioPhoneNumber,
                "callSid", call.getSid(),
                "provider", "twilio",
                "status", call.getStatus().toString(),
                "createdAt", LocalDateTime.now().toString()
        );
    }

    private URI buildTwimlUri(String callbackUrl) {
        try {
            return new URI(callbackUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid Twilio callback URL", e);
        }
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
