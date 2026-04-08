package org.bartnik.ingestor.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.StockDataBatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinnhubClient extends TextWebSocketHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.api.url}")
    private String apiUrl;

    @PostConstruct
    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        String url = String.format("%s?token=%s", apiUrl, apiKey);
        client.execute(this, url);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connected to Finnhub web socket");
        String subscriptionMessage = "{\"type\":\"subscribe\",\"symbol\":\"AAPL\"}";
        session.sendMessage(new TextMessage(subscriptionMessage));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("Received: {}", payload.substring(0, Math.min(payload.length(), 100)));

            StockDataBatch batch = objectMapper.readValue(payload, StockDataBatch.class);

            if (batch != null && "trade".equals(batch.type()) && batch.data() != null) {
                batch.data().forEach(trade -> {
                    if (trade.symbol() != null && trade.price() != null) {
                        kafkaTemplate.send("ticker-raw", trade.symbol(), trade);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
        }
    }
}
