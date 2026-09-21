package com.leadproject.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String email;

    @Column(nullable = false)
    private String industry;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String status = "NEW";

    private Long ownerId;
    private Integer score;
    private String scoreBand;

    @Column(length = 2000)
    private String summary;

    @Column(length = 500)
    private String leadIntent;

    @Column(length = 200)
    private String propertyType;

    @Column(length = 200)
    private String budgetRange;

    @Column(length = 200)
    private String preferredLocation;

    @Column(length = 200)
    private String timeline;

    @Column(length = 200)
    private String decisionMaker;

    @Column(length = 200)
    private String preferredCallbackTime;

    @Column(length = 3000)
    private String qualificationNotes;

    @Column(length = 100)
    private String callDisposition;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private ConsentStatus consentStatus = ConsentStatus.UNKNOWN;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum ConsentStatus {
        UNKNOWN,
        VALID,
        REVOKED,
        MISSING
    }
}
