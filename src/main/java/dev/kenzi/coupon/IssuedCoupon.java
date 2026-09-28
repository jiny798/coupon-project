package dev.kenzi.coupon;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class IssuedCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long couponId;

    private Long userId;

    private LocalDateTime issuedAt;

    protected IssuedCoupon() {
    }

    public IssuedCoupon(Long couponId, Long userId) {
        this.couponId = couponId;
        this.userId = userId;
        this.issuedAt = LocalDateTime.now();
    }
}
