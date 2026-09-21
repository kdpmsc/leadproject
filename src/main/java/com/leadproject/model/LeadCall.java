package com.leadproject.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lead_calls")
@Getter
@Setter
@NoArgsConstructor
public class LeadCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leadId;

    @Column(nullable = false)
    private String leadName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String status = "PLACED";

    @Column(length = 100)
    private String providerCallSid;

    @Column(length = 10000)
    private String transcript;

    @Column(length = 30000)
    private String conversation;

    @Column(length = 2000)
    private String summary;

    @Column(length = 500)
    private String recordingUrl;

    @Column(length = 100)
    private String recordingSid;

    @Column(length = 30)
    private String transcriptionStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}
