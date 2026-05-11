package org.bartnik.alerts.service;

import model.TradeAggregate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsBridgeService {

    private final SimpMessagingTemplate messagingTemplate;

    public AnalyticsBridgeService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "ticker-analytics",
            groupId = "alerts-websocket-bridge"
    )
    public void broadcastAnalytics(TradeAggregate aggregate) {
        System.out.println("DEBUG: Received from Kafka: " + aggregate.symbol());

        String destination = "/topic/analytics/" + aggregate.symbol();

        messagingTemplate.convertAndSend(destination, aggregate);

        System.out.println("DEBUG: Sent to WebSocket: " + destination);
    }
}
