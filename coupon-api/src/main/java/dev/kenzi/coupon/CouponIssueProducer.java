package dev.kenzi.coupon;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class CouponIssueProducer {

    private final List<KafkaTemplate<String, CouponIssueMessage>> kafkaTemplates;

    public CouponIssueProducer(ProducerFactory<String, CouponIssueMessage> producerFactory,
                               @Value("${coupon.kafka.producers}") int producers) {
        this.kafkaTemplates = IntStream.range(0, producers)
                .mapToObj(i -> new KafkaTemplate<>(producerFactory,
                        Map.<String, Object>of(ProducerConfig.CLIENT_ID_CONFIG, "coupon-api-" + i)))
                .toList();
    }

    public void send(CouponIssueMessage message) {
        int index = Math.floorMod(message.userId(), kafkaTemplates.size());
        kafkaTemplates.get(index)
                .send(CouponIssueMessage.TOPIC, String.valueOf(message.userId()), message)
                .join();
    }
}
