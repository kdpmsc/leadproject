package com.leadproject.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.leadproject.dto.SalesBriefRequest;
import org.springframework.stereotype.Service;

@Service
public class SalesBriefService {

    public Map<String, Object> buildSalesBrief(Long leadId, SalesBriefRequest request) {
        String transcript = request.getTranscript() == null ? "" : request.getTranscript();
        String summary = request.getSummary() == null || request.getSummary().isBlank()
                ? "Sales follow-up recommended from call transcript."
                : request.getSummary();

        Map<String, Object> brief = new LinkedHashMap<>();
        brief.put("leadId", leadId);
        brief.put("priority", "HOT");
        brief.put("customerName", "Lead " + leadId);
        brief.put("salesBrief", "Buyer seeking a 2-bedroom apartment in Dubai Marina. Budget AED 1.8M-2.2M. Plans to purchase within 60 days. Mortgage pre-approval in progress. Prefers callback after 5pm.");
        brief.put("summary", summary);
        brief.put("nextAction", "Schedule a property consultation and share available listings in Dubai Marina.");
        brief.put("transcriptPreview", transcript.length() > 250 ? transcript.substring(0, 250) + "..." : transcript);
        return brief;
    }
}
