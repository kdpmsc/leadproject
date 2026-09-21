package com.leadproject.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "playbook_versions")
@Getter
@Setter
@NoArgsConstructor
public class PlaybookVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playbook_id")
    private Playbook playbook;

    private String version;

    @Column(length = 10000)
    private String configurationJson;

    private String promptVersion;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String approvalState = "DRAFT";
    private LocalDateTime createdAt = LocalDateTime.now();
}
