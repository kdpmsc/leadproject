package com.leadproject.controller;

import com.leadproject.service.LeadService;
import com.leadproject.service.LeadCallService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class VoiceController {

    private final LeadService leadService;
    private final LeadCallService leadCallService;

    @Value("${twilio.app-base-url:https://leadproject-59dl.onrender.com}")
    private String appBaseUrl;

    public VoiceController(LeadService leadService, LeadCallService leadCallService) {
        this.leadService = leadService;
        this.leadCallService = leadCallService;
    }

        @RequestMapping(value = "/voice/property-qualification",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.TEXT_XML_VALUE)
    public String propertyQualificationTwiml(
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) String leadName) {

        return buildQualificationTwiml(leadId, leadName);
    }

        @RequestMapping(value = "/voice/inbound",
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.TEXT_XML_VALUE)
    public String inboundCallTwiml(
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) String leadName) {

        return buildQualificationTwiml(leadId, leadName);
    }

    @PostMapping(value = "/voice/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handleVoiceStatus(@RequestParam(required = false) String CallSid,
                                                @RequestParam(required = false) String CallUUID,
                                                @RequestParam(required = false) String CallStatus,
                                                @RequestParam(required = false) String To,
                                                @RequestParam(required = false) String From) {
        leadCallService.updateProviderStatus(CallSid != null ? CallSid : CallUUID, CallStatus);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/voice/answer", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_XML_VALUE)
    public String handleAnswer(@RequestParam Long leadId,
                               @RequestParam int question,
                       @RequestParam(required = false, defaultValue = "") String SpeechResult,
                               @RequestParam(required = false) String CallSid) {
        return processAnswer(leadId, question, SpeechResult, CallSid);
    }

    @PostMapping(value = "/voice/answer", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_XML_VALUE)
    public String handleJsonAnswer(@RequestBody java.util.Map<String, Object> request) {
        Long leadId = Long.valueOf(String.valueOf(request.get("leadId")));
        int question = Integer.parseInt(String.valueOf(request.get("question")));
        String speechResult = String.valueOf(request.getOrDefault("SpeechResult", ""));
        String callSid = request.get("CallSid") == null ? null : String.valueOf(request.get("CallSid"));
        return processAnswer(leadId, question, speechResult, callSid);
    }

    private String processAnswer(Long leadId, int question, String speechResult, String callSid) {
        leadCallService.appendAnswer(leadId, question, speechResult, callSid);

        return switch (question) {
            case 1 -> nextQuestion(leadId, 2, "Which area and property type are you considering?");
            case 2 -> nextQuestion(leadId, 3, "What budget range are you comfortable with?");
            case 3 -> nextQuestion(leadId, 4, "When do you plan to purchase?");
            case 4 -> nextQuestion(leadId, 5, "Are you the decision maker?");
            case 5 -> nextQuestion(leadId, 6, "When should a property consultant contact you?");
            default -> """
                    <Response><Say>Thank you. Our property consultant will contact you shortly. Goodbye.</Say></Response>
                    """;
        };
    }

    @PostMapping(value = "/voice/recording", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handleRecording(@RequestParam String CallSid,
                                                @RequestParam(required = false) String RecordingSid,
                                                @RequestParam(required = false) String RecordingUrl,
                                                @RequestParam(required = false) String RecordingStatus) {
        leadCallService.updateRecording(CallSid, RecordingSid, RecordingUrl, RecordingStatus);
        return ResponseEntity.ok().build();
    }

    private String buildQualificationTwiml(Long leadId, String leadName) {
        String safeName = escapeXml(leadName == null || leadName.isBlank() ? "lead" : leadName);

        String action = appBaseUrl + "/api/v1/voice/answer?leadId=" + leadId + "&question=1";
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Response>
                    <Say language="en-US">Hello %s, I am calling from XYZ Properties regarding your Dubai property inquiry.</Say>
                    <Gather input="speech" action="%s" method="POST" language="en-US" speechTimeout="auto">
                        <Say language="en-US">Are you looking to buy, rent, sell, or invest?</Say>
                    </Gather>
                    <Say language="en-US">We did not receive an answer. Goodbye.</Say>
                </Response>
                """.formatted(safeName, escapeXmlAttribute(action));
    }

    private String nextQuestion(Long leadId, int question, String text) {
        String action = appBaseUrl + "/api/v1/voice/answer?leadId=" + leadId + "&question=" + question;
        return """
                <Response>
                    <Gather input="speech" action="%s" method="POST" language="en-US" speechTimeout="auto">
                        <Say language="en-US">%s</Say>
                    </Gather>
                    <Say language="en-US">We did not receive an answer. Goodbye.</Say>
                </Response>
                """.formatted(escapeXmlAttribute(action), escapeXml(text));
    }

    private String escapeXmlAttribute(String value) {
        return escapeXml(value).replace("\"", "&quot;");
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
