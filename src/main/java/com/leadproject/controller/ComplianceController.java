package com.leadproject.controller;

import java.util.Map;

import com.leadproject.dto.SuppressionCreateRequest;
import com.leadproject.model.SuppressionEntry;
import com.leadproject.repository.SuppressionEntryRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ComplianceController {

    private final SuppressionEntryRepository suppressionEntryRepository;

    public ComplianceController(SuppressionEntryRepository suppressionEntryRepository) {
        this.suppressionEntryRepository = suppressionEntryRepository;
    }

    @PostMapping("/suppressions")
    public ResponseEntity<Map<String, Object>> addSuppression(@Valid @RequestBody SuppressionCreateRequest request) {
        SuppressionEntry entry = new SuppressionEntry();
        entry.setPhone(request.getPhone());
        entry.setReason(request.getReason());
        entry.setScope(request.getScope());
        entry.setSource(request.getSource());
        entry.setActive(true);

        suppressionEntryRepository.save(entry);

        return ResponseEntity.ok(Map.of(
                "phone", request.getPhone(),
                "reason", request.getReason(),
                "scope", request.getScope(),
                "status", "suppressed"
        ));
    }

    @GetMapping("/compliance/call-log")
    public ResponseEntity<Map<String, Object>> getCallLog() {
        return ResponseEntity.ok(Map.of(
                "message", "Call log export ready",
                "entries", 0,
                "from", "2026-09-01",
                "to", "2026-09-30"
        ));
    }
}
