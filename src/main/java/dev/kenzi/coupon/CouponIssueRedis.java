package dev.kenzi.coupon;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CouponIssueRedis {

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

    private String usersKey(Long couponId) {
        return "coupon:" + couponId + ":users";
    }

    private String countKey(Long couponId) {
        return "coupon:" + couponId + ":count";
    }
}
