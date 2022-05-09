package com.scb.job.kafka;

import com.scb.job.model.kafka.BroadcastNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationPublisherTest {

    private NotificationPublisher notificationPublisher;

    @Mock
    private KafkaTemplate<String, BroadcastNotification> kafkaTemplate;

    @BeforeEach
    public void setup(){
        notificationPublisher = new NotificationPublisher(kafkaTemplate, "topic");

    }

    @Test
    public void testSend(){

        BroadcastNotification broadcastNotification = BroadcastNotification.builder()
                .arn("arn")
                .platform("platform")
                .type("EVENT_TYPE")
                .build();

        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        when(kafkaTemplate.send(any(Message.class))).thenReturn(mockFuture);
        notificationPublisher.send(broadcastNotification);
        verify(kafkaTemplate, times(1))
                .send(any(Message.class));

    }
}
