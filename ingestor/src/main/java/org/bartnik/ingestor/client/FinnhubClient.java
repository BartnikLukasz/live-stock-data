package org.bartnik.ingestor.client;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import model.StockDataBatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Slf4j
@Service
public class FinnhubClient extends TextWebSocketHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.api.url}")
    private String apiUrl;

    public FinnhubClient(KafkaTemplate<String, Object> kafkaTemplate, JsonMapper jsonMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    @PostConstruct
    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        String url = String.format("%s?token=%s", apiUrl, apiKey);
        client.execute(this, url);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connected to Finnhub. Session ID: {}", session.getId());

        String subMsg = jsonMapper.writeValueAsString(Map.of(
                "type", "subscribe",
                "symbol", "BINANCE:BTCUSDT"
        ));

        log.info("Sending Subscription: {}", subMsg);
        session.sendMessage(new TextMessage(subMsg));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.info("Received: {}", payload);

            StockDataBatch batch = jsonMapper.readValue(payload, StockDataBatch.class);

            if (batch != null && "trade".equals(batch.type()) && batch.data() != null) {
                batch.data().forEach(trade -> {
                    if (trade.symbol() != null && trade.price() != null) {
                        log.info("Sending trade to Kafka: {}", trade.symbol());
                        kafkaTemplate.send("ticker-raw", trade.symbol(), trade);
                    }
                });
            } else {
                log.info("Received system messege: {}", batch);
            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getMessage());
        }
    }
}
