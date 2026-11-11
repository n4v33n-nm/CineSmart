package com.cinesmart.refund.repository;

import com.cinesmart.refund.entity.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {

    List<CancellationPolicy> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<CancellationPolicy> findAllByOrderByDisplayOrderAsc();

    Optional<CancellationPolicy> findByPolicyNameIgnoreCase(String policyName);

    @Query("SELECT p FROM CancellationPolicy p WHERE p.isActive = true ORDER BY p.minHoursBeforeShow DESC")
    List<CancellationPolicy> findActivePoliciesForEvaluation();
}

