package dev.kenzi.coupon;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class CouponIssueConsumer {

    private static final Logger log = LoggerFactory.getLogger(CouponIssueConsumer.class);

    private final IssuedCouponRepository issuedCouponRepository;
    private final Timer storeDelay;

    public CouponIssueConsumer(IssuedCouponRepository issuedCouponRepository, MeterRegistry meterRegistry) {
        this.issuedCouponRepository = issuedCouponRepository;
        this.storeDelay = Timer.builder("coupon.issue.store.delay")
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofMinutes(30))
                .register(meterRegistry);
    }

    @KafkaListener(topics = CouponIssueMessage.TOPIC, groupId = "coupon-issue-group")
    public void listen(CouponIssueMessage message,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long publishedAt,
                       Acknowledgment ack) {
        try {
            issuedCouponRepository.save(new IssuedCoupon(message.couponId(), message.userId()));
        } catch (DataIntegrityViolationException e) {
            log.warn("already issued: couponId={}, userId={}", message.couponId(), message.userId());
        }
        ack.acknowledge();
        storeDelay.record(System.currentTimeMillis() - publishedAt, TimeUnit.MILLISECONDS);
    }
}
