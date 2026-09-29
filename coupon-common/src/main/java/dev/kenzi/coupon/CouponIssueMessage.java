package dev.kenzi.coupon;

public record CouponIssueMessage(Long couponId, Long userId) {

    public static final String TOPIC = "coupon-issue";
}
