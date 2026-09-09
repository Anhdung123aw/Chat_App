package com.example.chatrealtime.consumer;

import com.example.chatrealtime.session.WebSocketSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chat.message.created", groupId = "chat-realtime-group")
    public void consumeMessageCreated(String messagePayload) {
        log.info("Received chat.message.created event: {}", messagePayload);
        try {
            JsonNode root = objectMapper.readTree(messagePayload);
            if (root.has("conversationId")) {
                String conversationId = root.get("conversationId").asText();
                // Broadcast directly as the payload is already in the expected WS JSON format
                sessionManager.broadcastToConversation(conversationId, messagePayload);
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }
}
