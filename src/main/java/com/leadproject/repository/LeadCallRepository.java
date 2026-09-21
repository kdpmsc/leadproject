package com.leadproject.repository;

import java.util.List;

import com.leadproject.model.LeadCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadCallRepository extends JpaRepository<LeadCall, Long> {
    List<LeadCall> findByLeadId(Long leadId);
    java.util.Optional<LeadCall> findByProviderCallSid(String providerCallSid);
    long countByStatus(String status);
}