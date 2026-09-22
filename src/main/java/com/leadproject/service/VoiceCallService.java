package com.leadproject.service;

import java.util.Map;

public interface VoiceCallService {
    Map<String, Object> placeCall(String toPhone, String leadName, Long leadId);

    default Map<String, Object> placeCall(String toPhone, String leadName, Long leadId, String type) {
        return placeCall(toPhone, leadName, leadId);
    }
}
