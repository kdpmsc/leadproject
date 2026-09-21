package com.leadproject.controller;

import java.util.Map;

import com.leadproject.dto.SalesBriefRequest;
import com.leadproject.service.SalesBriefService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SalesBriefController {

    private final SalesBriefService salesBriefService;

    public SalesBriefController(SalesBriefService salesBriefService) {
        this.salesBriefService = salesBriefService;
    }

    @PostMapping("/leads/{leadId}/sales-brief")
    public ResponseEntity<Map<String, Object>> buildSalesBrief(
            @PathVariable Long leadId,
            @Valid @RequestBody SalesBriefRequest request) {
        return ResponseEntity.ok(salesBriefService.buildSalesBrief(leadId, request));
    }
}
