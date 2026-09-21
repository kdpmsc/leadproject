package com.leadproject.repository;

import java.util.List;

import com.leadproject.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByStatus(String status);
    List<Lead> findByScoreBand(String scoreBand);
    boolean existsByPhone(String phone);
    java.util.Optional<Lead> findByPhone(String phone);
}
