package com.cinesmart.refund.repository;

import com.cinesmart.refund.entity.RefundRequest;
import com.cinesmart.refund.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    List<RefundRequest> findByUserIdOrderByRequestedAtDesc(Long userId);

    List<RefundRequest> findAllByOrderByRequestedAtDesc();

    List<RefundRequest> findByStatusOrderByRequestedAtDesc(RefundStatus status);

    Optional<RefundRequest> findByRefundReference(String refundReference);

    Optional<RefundRequest> findByBookingId(Long bookingId);

    boolean existsByBookingIdAndStatusNotIn(Long bookingId, List<RefundStatus> terminalStatuses);

    long countByStatus(RefundStatus status);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM RefundRequest r WHERE r.status = :status")
    BigDecimal sumRefundAmountByStatus(@Param("status") RefundStatus status);

    @Query("SELECT COALESCE(SUM(r.cancellationFee), 0) FROM RefundRequest r WHERE r.status = :status")
    BigDecimal sumCancellationFeeByStatus(@Param("status") RefundStatus status);
}

