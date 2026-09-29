package dev.kenzi.coupon;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CouponIssueProducer {

    private final KafkaTemplate<String, CouponIssueMessage> kafkaTemplate;

    public CouponIssueProducer(KafkaTemplate<String, CouponIssueMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(CouponIssueMessage message) {
        kafkaTemplate.send(CouponIssueMessage.TOPIC, String.valueOf(message.userId()), message).join();
    }
}
