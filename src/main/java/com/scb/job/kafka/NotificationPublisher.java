package com.scb.job.kafka;

import com.scb.job.model.kafka.BroadcastNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;


@Service
@Slf4j
public class NotificationPublisher {

    private KafkaTemplate<String, BroadcastNotification> kafkaTemplate;

    private String topic;

    @Autowired
    public NotificationPublisher(KafkaTemplate<String, BroadcastNotification> kafkaTemplate,
                                 @Value("${kafka.notification-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(BroadcastNotification data) {
        log.info("sending data to topic='{}'", topic);

        Message<BroadcastNotification> message = MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
        ListenableFuture<SendResult<String, BroadcastNotification>> future = kafkaTemplate.send(message);
        future.addCallback(callback());
    }

    private ListenableFutureCallback<? super SendResult<String, BroadcastNotification>> callback() {
        return new ListenableFutureCallback<SendResult<String, BroadcastNotification>>() {

            @Override
            public void onSuccess(SendResult<String, BroadcastNotification> result) {
                log.info("Message published successfully");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while publishing message.", ex);
            }

        };

    }
}