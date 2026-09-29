package dev.kenzi.coupon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class CouponIssueConsumer {

    private static final Logger log = LoggerFactory.getLogger(CouponIssueConsumer.class);

    private final IssuedCouponRepository issuedCouponRepository;

    public CouponIssueConsumer(IssuedCouponRepository issuedCouponRepository) {
        this.issuedCouponRepository = issuedCouponRepository;
    }

    @KafkaListener(topics = CouponIssueMessage.TOPIC, groupId = "coupon-issue-group")
    public void listen(CouponIssueMessage message, Acknowledgment ack) {
        try {
            issuedCouponRepository.save(new IssuedCoupon(message.couponId(), message.userId()));
        } catch (DataIntegrityViolationException e) {
            log.warn("already issued: couponId={}, userId={}", message.couponId(), message.userId());
        }
        ack.acknowledge();
    }
}
