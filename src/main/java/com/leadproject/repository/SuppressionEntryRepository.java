package com.leadproject.repository;

import com.leadproject.model.SuppressionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuppressionEntryRepository extends JpaRepository<SuppressionEntry, Long> {
    boolean existsByPhoneAndActiveTrue(String phone);
}
