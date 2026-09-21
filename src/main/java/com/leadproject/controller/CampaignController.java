package com.leadproject.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.leadproject.dto.CampaignCreateRequest;
import com.leadproject.model.Campaign;
import com.leadproject.repository.CampaignRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CampaignController {

    private final CampaignRepository campaignRepository;

    public CampaignController(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @PostMapping("/campaigns")
    public ResponseEntity<Campaign> createCampaign(@Valid @RequestBody CampaignCreateRequest request) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setPlaybookVersionId(request.getPlaybookVersionId());
        campaign.setStatus("DRAFT");
        campaign.setCallWindow(request.getCallSchedule() != null
                ? request.getCallSchedule().getStart() + "-" + request.getCallSchedule().getEnd()
                : "09:00-18:00");
        campaign.setRetryPolicy(request.getRetryPolicy() != null
                ? "max_attempts=" + request.getRetryPolicy().getMaxAttempts()
                : "max_attempts=2");
        campaign.setOwnerId(1L);
        campaign.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(campaignRepository.save(campaign));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<Campaign>> getCampaigns() {
        return ResponseEntity.ok(campaignRepository.findAll());
    }
}
