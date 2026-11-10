package com.cinesmart.coupon.repository;

import com.cinesmart.coupon.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    int countByCouponIdAndUserId(Long couponId, Long userId);

    Optional<CouponUsage> findByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);
}

