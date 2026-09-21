package com.leadproject.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "suppression_entries")
@Getter
@Setter
@NoArgsConstructor
public class SuppressionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    private String reason;
    private String scope = "ALL_CAMPAIGNS";
    private String source;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
