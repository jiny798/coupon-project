package dev.kenzi.coupon;

import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(@Value("${spring.data.redis.host:localhost}") String host,
                                         @Value("${spring.data.redis.port:6379}") int port) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }

    @Bean
    public RRateLimiter couponIssueRateLimiter(RedissonClient redissonClient,
                                               @Value("${coupon.issue.rate-limit}") long permitsPerSecond) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("coupon:issue:rate-limiter");
        rateLimiter.setRate(RateType.OVERALL, permitsPerSecond, 1, RateIntervalUnit.SECONDS);
        return rateLimiter;
    }
}
