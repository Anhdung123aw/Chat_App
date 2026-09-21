package com.example.realtime.consumer;

import com.example.realtime.dto.WebSocketMessage;
import com.example.realtime.service.RedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer for Message Created Events
 * Listens to topic: chat.message.created
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCreatedConsumer {

    private final RedisMessagePublisher redisPublisher;

    @KafkaListener(
            topics = "${chat.realtime.kafka.topics.message-created:chat.message.created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Map<String, Object> eventMap) {
        try {
            if (eventMap == null) {
                log.warn("Received null eventMap from Kafka (possibly due to deserialization error), skipping.");
                return;
            }
            String messageId    = str(eventMap, "messageId");
            String conversationId = str(eventMap, "conversationId");
            String senderId     = str(eventMap, "senderId");
            String senderType   = str(eventMap, "senderType");
            String messageType  = str(eventMap, "messageType");
            String content      = str(eventMap, "content");
            Object seqObj       = eventMap.get("sequence");
            Long sequence       = seqObj != null ? ((Number) seqObj).longValue() : null;

            log.info("Received MessageCreatedEvent - messageId: {}, conversationId: {}, senderId: {}",
                    messageId, conversationId, senderId);

            // Build payload for WebSocket
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", messageId);
            payload.put("messageId", messageId);
            payload.put("conversationId", conversationId);
            payload.put("content", content);
            payload.put("messageType", messageType);
            payload.put("senderId", senderId);
            payload.put("senderType", senderType);
            payload.put("sequence", sequence);
            payload.put("createdAt", str(eventMap, "timestamp"));

            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.MESSAGE)
                    .eventId(messageId)
                    .conversationId(conversationId)
                    .sequence(sequence)
                    .timestamp(LocalDateTime.now())
                    .payload(payload)
                    .senderId(senderId)
                    .senderType(senderType)
                    .build();

            redisPublisher.publishMessage(conversationId, wsMessage);

            log.debug("Published message to Redis - conversationId: {}, messageId: {}", conversationId, messageId);

        } catch (Exception e) {
            log.error("Error processing MessageCreatedEvent", e);
        }
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
