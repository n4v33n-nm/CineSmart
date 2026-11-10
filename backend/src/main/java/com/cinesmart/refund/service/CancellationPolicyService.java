package com.cinesmart.refund.service;

import com.cinesmart.common.exception.BadRequestException;
import com.cinesmart.common.exception.ResourceNotFoundException;
import com.cinesmart.refund.dto.CancellationPolicyCreateRequest;
import com.cinesmart.refund.dto.CancellationPolicyDTO;
import com.cinesmart.refund.entity.CancellationPolicy;
import com.cinesmart.refund.repository.CancellationPolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CancellationPolicyService {

    private static final Logger log = LoggerFactory.getLogger(CancellationPolicyService.class);

    private final CancellationPolicyRepository cancellationPolicyRepository;

    public CancellationPolicyService(CancellationPolicyRepository cancellationPolicyRepository) {
        this.cancellationPolicyRepository = cancellationPolicyRepository;
    }

    public List<CancellationPolicyDTO> getAllPolicies() {
        return cancellationPolicyRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(CancellationPolicyDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CancellationPolicyDTO> getActivePolicies() {
        return cancellationPolicyRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(CancellationPolicyDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public CancellationPolicyDTO getPolicyById(Long id) {
        CancellationPolicy policy = cancellationPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancellationPolicy", "id", id));
        return CancellationPolicyDTO.fromEntity(policy);
    }

    @Transactional
    public CancellationPolicyDTO createPolicy(CancellationPolicyCreateRequest request) {
        Optional<CancellationPolicy> existing = cancellationPolicyRepository.findByPolicyNameIgnoreCase(request.getPolicyName().trim());
        if (existing.isPresent()) {
            throw new BadRequestException("A cancellation policy with name '" + request.getPolicyName() + "' already exists", "DUPLICATE_POLICY_NAME");
        }

        CancellationPolicy policy = new CancellationPolicy(
                request.getPolicyName().trim(),
                request.getDescription(),
                request.getMinHoursBeforeShow(),
                request.getMaxHoursBeforeShow(),
                request.getRefundPercentage(),
                request.getCancellationFeePercent(),
                request.getDisplayOrder()
        );

        if (request.getIsActive() != null) {
            policy.setIsActive(request.getIsActive());
        }

        CancellationPolicy saved = cancellationPolicyRepository.save(policy);
        log.info("Created cancellation policy: {} (id: {})", saved.getPolicyName(), saved.getId());
        return CancellationPolicyDTO.fromEntity(saved);
    }

    @Transactional
    public CancellationPolicyDTO updatePolicy(Long id, CancellationPolicyCreateRequest request) {
        CancellationPolicy policy = cancellationPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancellationPolicy", "id", id));

        Optional<CancellationPolicy> duplicate = cancellationPolicyRepository.findByPolicyNameIgnoreCase(request.getPolicyName().trim());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new BadRequestException("Another policy with name '" + request.getPolicyName() + "' already exists", "DUPLICATE_POLICY_NAME");
        }

        policy.setPolicyName(request.getPolicyName().trim());
        policy.setDescription(request.getDescription());
        policy.setMinHoursBeforeShow(request.getMinHoursBeforeShow());
        policy.setMaxHoursBeforeShow(request.getMaxHoursBeforeShow());
        policy.setRefundPercentage(request.getRefundPercentage());
        policy.setCancellationFeePercent(request.getCancellationFeePercent());
        if (request.getIsActive() != null) {
            policy.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            policy.setDisplayOrder(request.getDisplayOrder());
        }

        CancellationPolicy updated = cancellationPolicyRepository.save(policy);
        log.info("Updated cancellation policy: {} (id: {})", updated.getPolicyName(), updated.getId());
        return CancellationPolicyDTO.fromEntity(updated);
    }

    @Transactional
    public CancellationPolicyDTO togglePolicyStatus(Long id) {
        CancellationPolicy policy = cancellationPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancellationPolicy", "id", id));
        policy.setIsActive(!Boolean.TRUE.equals(policy.getIsActive()));
        CancellationPolicy updated = cancellationPolicyRepository.save(policy);
        log.info("Toggled cancellation policy status: id {} is now {}", id, updated.getIsActive());
        return CancellationPolicyDTO.fromEntity(updated);
    }

    @Transactional
    public void deletePolicy(Long id) {
        CancellationPolicy policy = cancellationPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CancellationPolicy", "id", id));
        // Soft delete / deactivate to maintain foreign key integrity with existing refund requests
        policy.setIsActive(false);
        cancellationPolicyRepository.save(policy);
        log.info("Deactivated cancellation policy id {}", id);
    }

    /**
     * Finds the applicable active cancellation policy for the given window of hours before show.
     */
    public CancellationPolicy findMatchingPolicy(double hoursBeforeShow) {
        List<CancellationPolicy> activePolicies = cancellationPolicyRepository.findActivePoliciesForEvaluation();
        for (CancellationPolicy policy : activePolicies) {
            if (policy.matches(hoursBeforeShow)) {
                return policy;
            }
        }

        // Fallback default: if show is in the past (< 0 hours) or very late, no refund
        if (hoursBeforeShow <= 0) {
            return new CancellationPolicy("No Refund (Show Started)", "Show has already started or concluded", 0, 0, BigDecimal.ZERO, new BigDecimal("100.00"), 999);
        }

        // If hours before show exceeds highest policy threshold, return 100% refund
        return new CancellationPolicy("Default Full Refund", "Standard early cancellation", 0, null, new BigDecimal("100.00"), BigDecimal.ZERO, 0);
    }
}
