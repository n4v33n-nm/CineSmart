package com.cinesmart.payment.repository;

import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByTransactionReference(String transactionReference);

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    List<PaymentTransaction> findByBookingId(Long bookingId);

    Optional<PaymentTransaction> findByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
