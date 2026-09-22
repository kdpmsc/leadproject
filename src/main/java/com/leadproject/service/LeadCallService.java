package com.leadproject.service;

import java.time.LocalDateTime;
import java.util.List;

import com.leadproject.model.Lead;
import com.leadproject.model.LeadCall;
import com.leadproject.repository.LeadCallRepository;
import com.leadproject.repository.LeadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadCallService {

    private static final Logger logger = LoggerFactory.getLogger(LeadCallService.class);

    private final LeadCallRepository leadCallRepository;
    private final LeadRepository leadRepository;

    public LeadCallService(LeadCallRepository leadCallRepository, LeadRepository leadRepository) {
        this.leadCallRepository = leadCallRepository;
        this.leadRepository = leadRepository;
    }

    @Transactional
    public LeadCall createCall(Long leadId, String phone, String leadName) {
        logger.info("Creating lead call: leadId={}, phone={}, leadName={}", leadId, phone, leadName);
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));

        LeadCall call = new LeadCall();
        call.setLeadId(lead.getId());
        call.setLeadName(lead.getName());
        call.setPhone(lead.getPhone());
        call.setStatus("PLACED");
        call.setCreatedAt(LocalDateTime.now());
        call.setUpdatedAt(LocalDateTime.now());
        LeadCall savedCall = leadCallRepository.save(call);
        logger.info("Lead call created: callId={}, leadId={}", savedCall.getId(), leadId);
        return savedCall;
    }

    @Transactional
    public LeadCall updateCall(Long callId, String transcript, String recordingUrl, String summary, String status) {
        logger.info("Updating lead call: callId={}, transcriptLength={}, status={}", callId,
            transcript == null ? 0 : transcript.length(), status);
        LeadCall call = leadCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Lead call not found: " + callId));

        call.setTranscript(transcript);
        call.setRecordingUrl(recordingUrl);
        call.setSummary(summary);
        call.setStatus(status != null ? status : "COMPLETED");
        call.setUpdatedAt(LocalDateTime.now());
        return leadCallRepository.save(call);
    }

    @Transactional
    public LeadCall attachProviderCall(Long callId, String providerCallSid) {
        logger.info("Attaching provider call: callId={}, providerCallSid={}", callId, providerCallSid);
        LeadCall call = leadCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Lead call not found: " + callId));
        call.setProviderCallSid(providerCallSid);
        call.setStatus("QUEUED");
        call.setUpdatedAt(LocalDateTime.now());
        return leadCallRepository.save(call);
    }

    @Transactional
    public LeadCall appendAnswer(Long leadId, int questionNumber, String answer, String providerCallSid) {
        logger.info("Appending call answer: leadId={}, question={}, providerCallSid={}, answerReceived={}",
            leadId, questionNumber, providerCallSid, answer != null && !answer.isBlank());
        LeadCall call = providerCallSid == null || providerCallSid.isBlank()
                ? leadCallRepository.findByLeadId(leadId).stream().reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalArgumentException("Lead call not found: " + leadId))
                : leadCallRepository.findByProviderCallSid(providerCallSid)
                .orElseThrow(() -> new IllegalArgumentException("Lead call not found for provider SID: " + providerCallSid));

        String entry = "Q" + questionNumber + ": " + answer;
        String conversation = call.getConversation();
        call.setConversation(conversation == null || conversation.isBlank()
                ? entry
                : conversation + "\n" + entry);
        call.setTranscript(call.getConversation());
        call.setUpdatedAt(LocalDateTime.now());
        return leadCallRepository.save(call);
    }

    @Transactional(readOnly = true)
    public String getConversation(Long leadId, String providerCallSid) {
        LeadCall call = findCall(leadId, providerCallSid);
        return call.getConversation() == null ? "" : call.getConversation();
    }

    @Transactional
    public LeadCall appendAgentTurn(Long leadId, String providerCallSid, String role, String text) {
        LeadCall call = findCall(leadId, providerCallSid);
        String entry = role.toUpperCase() + ": " + text;
        String conversation = call.getConversation();
        call.setConversation(conversation == null || conversation.isBlank()
                ? entry
                : conversation + "\n" + entry);
        call.setTranscript(call.getConversation());
        call.setUpdatedAt(LocalDateTime.now());
        return leadCallRepository.save(call);
    }

    private LeadCall findCall(Long leadId, String providerCallSid) {
        return providerCallSid == null || providerCallSid.isBlank()
                ? leadCallRepository.findByLeadId(leadId).stream().reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalArgumentException("Lead call not found: " + leadId))
                : leadCallRepository.findByProviderCallSid(providerCallSid)
                .orElseThrow(() -> new IllegalArgumentException("Lead call not found for provider SID: " + providerCallSid));
    }

    @Transactional
    public LeadCall updateRecording(String providerCallSid, String recordingSid, String recordingUrl, String status) {
        logger.info("Updating call recording: providerCallSid={}, recordingSid={}, status={}",
            providerCallSid, recordingSid, status);
        LeadCall call = leadCallRepository.findByProviderCallSid(providerCallSid)
                .orElseThrow(() -> new IllegalArgumentException("Lead call not found for provider SID: " + providerCallSid));
        call.setRecordingSid(recordingSid);
        call.setRecordingUrl(recordingUrl);
        call.setTranscriptionStatus(status == null ? "PENDING" : status.toUpperCase());
        call.setUpdatedAt(LocalDateTime.now());
        return leadCallRepository.save(call);
    }

    @Transactional
    public void updateProviderStatus(String providerCallSid, String status) {
        logger.info("Updating provider call status: providerCallSid={}, status={}", providerCallSid, status);
        leadCallRepository.findByProviderCallSid(providerCallSid).ifPresent(call -> {
            call.setStatus(status == null ? "UNKNOWN" : status.toUpperCase());
            call.setUpdatedAt(LocalDateTime.now());
            leadCallRepository.save(call);
        });
    }

    @Transactional(readOnly = true)
    public List<LeadCall> getCallHistory(Long leadId) {
        logger.info("Loading call history: leadId={}", leadId);
        return leadCallRepository.findByLeadId(leadId);
    }
}
