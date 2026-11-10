package com.cinesmart.loyalty.repository;

import com.cinesmart.loyalty.entity.LoyaltyTransaction;
import com.cinesmart.loyalty.entity.LoyaltyTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    List<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByBookingIdAndTransactionType(Long bookingId, LoyaltyTransactionType transactionType);

    Optional<LoyaltyTransaction> findByBookingIdAndTransactionType(Long bookingId, LoyaltyTransactionType transactionType);

    List<LoyaltyTransaction> findByBookingId(Long bookingId);
}
