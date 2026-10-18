package dev.kenzi.coupon;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CouponIssueConsumer {

    private static final String INSERT_IGNORE =
            "INSERT IGNORE INTO issued_coupon (coupon_id, user_id, issued_at) VALUES (?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final Timer storeDelay;
    private final Counter stored;

    public CouponIssueConsumer(JdbcTemplate jdbcTemplate, MeterRegistry meterRegistry) {
        this.jdbcTemplate = jdbcTemplate;
        this.storeDelay = Timer.builder("coupon.issue.store.delay")
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofMinutes(30))
                .register(meterRegistry);
        this.stored = Counter.builder("coupon.issue.stored").register(meterRegistry);
    }

    @KafkaListener(topics = CouponIssueMessage.TOPIC, groupId = "coupon-issue-group")
    public void listen(List<ConsumerRecord<String, CouponIssueMessage>> records, Acknowledgment ack) {
        Timestamp issuedAt = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.batchUpdate(INSERT_IGNORE, records, records.size(), (ps, record) -> {
            ps.setLong(1, record.value().couponId());
            ps.setLong(2, record.value().userId());
            ps.setTimestamp(3, issuedAt);
        });
        ack.acknowledge();
        long now = System.currentTimeMillis();
        records.forEach(record -> storeDelay.record(now - record.timestamp(), TimeUnit.MILLISECONDS));
        stored.increment(records.size());
    }
}
