package com.example.realtime.consumer;

import com.example.realtime.dto.WebSocketMessage;
import com.example.realtime.service.RedisMessagePublisher;
import com.example.realtime.service.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer for Conversation Status Changed Events
 * Listens to topic: chat.conversation.status-changed
 * 
 * When a conversation status changes (e.g., OPEN -> CLOSED),
 * this consumer notifies all participants via WebSocket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationStatusChangedConsumer {

    private final RedisMessagePublisher redisPublisher;
    private final WebSocketMessageSender messageSender;

    @KafkaListener(
            topics = "${chat.realtime.kafka.topics.conversation-status-changed:chat.conversation.status-changed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Map<String, Object> eventMap) {
        try {
            String conversationId = str(eventMap, "conversationId");
            String agentId        = str(eventMap, "agentId");
            String customerId     = str(eventMap, "userId");
            String newStatus      = str(eventMap, "newStatus");

            log.info("Received ConversationStatusChangedEvent - conversationId: {}, newStatus: {}",
                    conversationId, newStatus);

            Map<String, Object> payload = new HashMap<>(eventMap);

            WebSocketMessage.MessageType messageType = "CLOSED".equals(newStatus)
                    ? WebSocketMessage.MessageType.CONVERSATION_CLOSED
                    : WebSocketMessage.MessageType.CONVERSATION_STATUS_CHANGED;

            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type(messageType)
                    .eventId(conversationId + "-" + newStatus)
                    .conversationId(conversationId)
                    .timestamp(LocalDateTime.now())
                    .payload(payload)
                    .senderId("SYSTEM")
                    .senderType("SYSTEM")
                    .build();

            // Broadcast to conversation channel
            redisPublisher.publishMessage(conversationId, wsMessage);

            // Notify specific users
            if (agentId != null) messageSender.sendToUser(agentId, wsMessage);
            if (customerId != null) messageSender.sendToUser(customerId, wsMessage);

            log.debug("Notified about conversation status change - conversationId: {}, newStatus: {}",
                    conversationId, newStatus);

        } catch (Exception e) {
            log.error("Error processing ConversationStatusChangedEvent", e);
        }
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
