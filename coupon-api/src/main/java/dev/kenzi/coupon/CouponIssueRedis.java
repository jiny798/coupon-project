package dev.kenzi.coupon;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CouponIssueRedis {

    private static final Duration QUANTITY_TTL = Duration.ofHours(12);

    private final StringRedisTemplate redisTemplate;

    public CouponIssueRedis(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean addUser(Long couponId, Long userId) {
        Long added = redisTemplate.opsForSet().add(usersKey(couponId), String.valueOf(userId));
        return added != null && added == 1;
    }

    public void removeUser(Long couponId, Long userId) {
        redisTemplate.opsForSet().remove(usersKey(couponId), String.valueOf(userId));
    }

    public long increment(Long couponId) {
        return redisTemplate.opsForValue().increment(countKey(couponId));
    }

    public Integer findTotalQuantity(Long couponId) {
        String value = redisTemplate.opsForValue().get(quantityKey(couponId));
        return value == null ? null : Integer.valueOf(value);
    }

    public void saveTotalQuantity(Long couponId, int totalQuantity) {
        redisTemplate.opsForValue().set(quantityKey(couponId), String.valueOf(totalQuantity), QUANTITY_TTL);
    }

    private String usersKey(Long couponId) {
        return "coupon:" + couponId + ":users";
    }

    private String countKey(Long couponId) {
        return "coupon:" + couponId + ":count";
    }

    private String quantityKey(Long couponId) {
        return "coupon:" + couponId + ":quantity";
    }
}
