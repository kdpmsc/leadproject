package com.leadproject.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.leadproject.dto.LeadCreateRequest;
import com.leadproject.model.Lead;
import com.leadproject.repository.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadService {

    private static final Logger logger = LoggerFactory.getLogger(LeadService.class);

    private final LeadRepository leadRepository;

    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Transactional
    public Lead createLead(LeadCreateRequest request) {
        logger.info("Creating lead: phone={}, name={}", request.getPhone(), request.getName());
        String phone = normalizePhone(request.getPhone());
        if (leadRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Lead with this phone number already exists");
        }

        Lead lead = new Lead();
        lead.setName(request.getName());
        lead.setPhone(phone);
        lead.setEmail(request.getEmail());
        lead.setIndustry(request.getIndustry());
        lead.setSource(request.getSource());
        lead.setLeadIntent(request.getIntent());
        lead.setPropertyType(request.getPropertyType());
        lead.setBudgetRange(request.getBudgetRange());
        lead.setPreferredLocation(request.getPreferredLocation());
        lead.setTimeline(request.getTimeline());
        lead.setDecisionMaker(request.getDecisionMaker());
        lead.setPreferredCallbackTime(request.getPreferredCallbackTime());
        lead.setQualificationNotes(request.getQualificationNotes());
        lead.setStatus("NEW");
        lead.setConsentStatus(Lead.ConsentStatus.VALID);
        lead.setScore(0);
        lead.setScoreBand("COLD");
        lead.setSummary("Awaiting qualification call");
        lead.setUpdatedAt(LocalDateTime.now());

        Lead savedLead = leadRepository.save(lead);
        logger.info("Lead created: leadId={}, phone={}", savedLead.getId(), savedLead.getPhone());
        return savedLead;
    }

    @Transactional(readOnly = true)
    public List<Lead> listLeads(String status, String scoreBand) {
        logger.info("Listing leads: status={}, scoreBand={}", status, scoreBand);
        if (status != null && !status.isBlank()) {
            return leadRepository.findByStatus(status.toUpperCase());
        }
        if (scoreBand != null && !scoreBand.isBlank()) {
            return leadRepository.findByScoreBand(scoreBand.toUpperCase());
        }
        return leadRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Lead getLead(Long leadId) {
        logger.info("Loading lead: leadId={}", leadId);
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
    }

    @Transactional(readOnly = true)
    public Optional<Lead> findLead(Long leadId) {
        logger.debug("Finding lead: leadId={}", leadId);
        return leadRepository.findById(leadId);
    }

    @Transactional(readOnly = true)
    public Lead getLeadByPhone(String phone) {
        logger.info("Loading lead by phone: phone={}", phone);
        String normalizedPhone = normalizePhone(phone);
        return leadRepository.findByPhone(normalizedPhone)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found for phone: " + phone));
    }

    @Transactional
    public Lead getOrCreateCallLead(String phone, String leadName) {
        logger.info("Getting or creating call lead: phone={}, leadName={}", phone, leadName);
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone.isBlank()) {
            throw new IllegalArgumentException("Phone is required to start a call");
        }

        return leadRepository.findByPhone(normalizedPhone).orElseGet(() -> {
            Lead lead = new Lead();
            lead.setName(leadName == null || leadName.isBlank() ? "Unknown lead" : leadName.trim());
            lead.setPhone(normalizedPhone);
            lead.setIndustry("unknown");
            lead.setSource("voice_call");
            lead.setStatus("NEW");
            lead.setConsentStatus(Lead.ConsentStatus.UNKNOWN);
            lead.setScore(0);
            lead.setScoreBand("COLD");
            lead.setSummary("Created when the call was started");
            lead.setUpdatedAt(LocalDateTime.now());
            return leadRepository.save(lead);
        });
    }

    @Transactional
    public Lead assignLead(Long leadId, Long userId, String reason) {
        logger.info("Assigning lead: leadId={}, userId={}", leadId, userId);
        Lead lead = getLead(leadId);
        lead.setOwnerId(userId);
        lead.setStatus("QUEUED");
        lead.setSummary("Assigned for follow-up: " + reason);
        lead.setUpdatedAt(LocalDateTime.now());
        return leadRepository.save(lead);
    }

    @Transactional
    public Lead qualifyLead(Long leadId, String intent, String propertyType, String budgetRange,
                           String preferredLocation, String timeline, String decisionMaker,
                           String preferredCallbackTime, String qualificationNotes) {
                logger.info("Qualifying lead: leadId={}, intent={}, propertyType={}", leadId, intent, propertyType);
        Lead lead = getLead(leadId);

        lead.setLeadIntent(intent);
        lead.setPropertyType(propertyType);
        lead.setBudgetRange(budgetRange);
        lead.setPreferredLocation(preferredLocation);
        lead.setTimeline(timeline);
        lead.setDecisionMaker(decisionMaker);
        lead.setPreferredCallbackTime(preferredCallbackTime);
        lead.setQualificationNotes(qualificationNotes);

        int score = 0;
        if (intent != null && !intent.isBlank()) score += 20;
        if (propertyType != null && !propertyType.isBlank()) score += 15;
        if (budgetRange != null && !budgetRange.isBlank()) score += 20;
        if (preferredLocation != null && !preferredLocation.isBlank()) score += 10;
        if (timeline != null && !timeline.isBlank()) score += 15;
        if (decisionMaker != null && !decisionMaker.isBlank()) score += 10;
        if (preferredCallbackTime != null && !preferredCallbackTime.isBlank()) score += 10;

        lead.setScore(score);
        if (score >= 75) {
            lead.setScoreBand("HOT");
            lead.setStatus("QUALIFIED");
            lead.setSummary("Hot real-estate lead ready for sales handoff");
            lead.setCallDisposition("qualified");
        } else if (score >= 45) {
            lead.setScoreBand("WARM");
            lead.setStatus("FOLLOW_UP");
            lead.setSummary("Warm lead requiring a callback and deeper discovery");
            lead.setCallDisposition("callback_requested");
        } else {
            lead.setScoreBand("COLD");
            lead.setStatus("NURTURE");
            lead.setSummary("Low-intent lead; nurture later if permitted");
            lead.setCallDisposition("nurture");
        }

        lead.setUpdatedAt(LocalDateTime.now());
        Lead savedLead = leadRepository.save(lead);
        logger.info("Lead qualified: leadId={}, score={}, scoreBand={}, status={}",
            leadId, savedLead.getScore(), savedLead.getScoreBand(), savedLead.getStatus());
        return savedLead;
    }

    @Transactional(readOnly = true)
    public Optional<Lead> findByPhone(String phone) {
        logger.debug("Finding lead by phone: phone={}", phone);
        return leadRepository.findByPhone(phone);
    }

    public String normalizePhone(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            return normalized;
        }
        normalized = normalized.replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }
        return normalized;
    }
}
