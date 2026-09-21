package com.leadproject.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.leadproject.dto.PlaybookCreateRequest;
import com.leadproject.model.Playbook;
import com.leadproject.repository.PlaybookRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PlaybookController {

    private final PlaybookRepository playbookRepository;

    public PlaybookController(PlaybookRepository playbookRepository) {
        this.playbookRepository = playbookRepository;
    }

    @PostMapping("/playbooks")
    public ResponseEntity<Playbook> createPlaybook(@Valid @RequestBody PlaybookCreateRequest request) {
        Playbook playbook = new Playbook();
        playbook.setIndustry(request.getIndustry());
        playbook.setName(request.getName());
        playbook.setStatus(request.getStatus());
        playbook.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(playbookRepository.save(playbook));
    }

    @GetMapping("/playbooks")
    public ResponseEntity<List<Playbook>> getPlaybooks() {
        return ResponseEntity.ok(playbookRepository.findAll());
    }
}
