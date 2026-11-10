package com.cinesmart.loyalty.entity;

import com.cinesmart.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_accounts")
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "points_balance", nullable = false)
    private Integer pointsBalance = 0;

    @Column(name = "lifetime_points_earned", nullable = false)
    private Integer lifetimePointsEarned = 0;

    @Column(name = "lifetime_points_redeemed", nullable = false)
    private Integer lifetimePointsRedeemed = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public LoyaltyAccount() {
    }

    public LoyaltyAccount(User user) {
        this.user = user;
        this.pointsBalance = 0;
        this.lifetimePointsEarned = 0;
        this.lifetimePointsRedeemed = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public void addPoints(int points) {
        if (points > 0) {
            this.pointsBalance = (this.pointsBalance != null ? this.pointsBalance : 0) + points;
            this.lifetimePointsEarned = (this.lifetimePointsEarned != null ? this.lifetimePointsEarned : 0) + points;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void deductPoints(int points) {
        if (points > 0) {
            int current = this.pointsBalance != null ? this.pointsBalance : 0;
            this.pointsBalance = Math.max(0, current - points);
            this.lifetimePointsRedeemed = (this.lifetimePointsRedeemed != null ? this.lifetimePointsRedeemed : 0) + points;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void reverseClawback(int earnedPointsToClawback) {
        if (earnedPointsToClawback > 0) {
            int current = this.pointsBalance != null ? this.pointsBalance : 0;
            this.pointsBalance = Math.max(0, current - earnedPointsToClawback);
            this.lifetimePointsEarned = Math.max(0, (this.lifetimePointsEarned != null ? this.lifetimePointsEarned : 0) - earnedPointsToClawback);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void restoreRedeemed(int redeemedPointsToRestore) {
        if (redeemedPointsToRestore > 0) {
            this.pointsBalance = (this.pointsBalance != null ? this.pointsBalance : 0) + redeemedPointsToRestore;
            this.lifetimePointsRedeemed = Math.max(0, (this.lifetimePointsRedeemed != null ? this.lifetimePointsRedeemed : 0) - redeemedPointsToRestore);
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getPointsBalance() {
        return pointsBalance != null ? pointsBalance : 0;
    }

    public void setPointsBalance(Integer pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public Integer getLifetimePointsEarned() {
        return lifetimePointsEarned != null ? lifetimePointsEarned : 0;
    }

    public void setLifetimePointsEarned(Integer lifetimePointsEarned) {
        this.lifetimePointsEarned = lifetimePointsEarned;
    }

    public Integer getLifetimePointsRedeemed() {
        return lifetimePointsRedeemed != null ? lifetimePointsRedeemed : 0;
    }

    public void setLifetimePointsRedeemed(Integer lifetimePointsRedeemed) {
        this.lifetimePointsRedeemed = lifetimePointsRedeemed;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
