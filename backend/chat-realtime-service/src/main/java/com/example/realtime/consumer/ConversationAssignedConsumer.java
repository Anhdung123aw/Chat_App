package com.example.realtime.consumer;

import com.example.realtime.dto.WebSocketMessage;
import com.example.realtime.service.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer for Conversation Assigned Events
 * Listens to topic: chat.conversation.assigned
 * 
 * When a conversation is assigned to an agent,
 * this consumer notifies the agent via WebSocket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationAssignedConsumer {

    private final WebSocketMessageSender messageSender;

    @KafkaListener(
            topics = "${chat.realtime.kafka.topics.conversation-assigned:chat.conversation.assigned}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Map<String, Object> eventMap) {
        try {
            String conversationId = str(eventMap, "conversationId");
            String agentId        = str(eventMap, "agentId");
            String customerId     = str(eventMap, "userId");

            log.info("Received ConversationAssignedEvent - conversationId: {}, agentId: {}",
                    conversationId, agentId);

            Map<String, Object> payload = new HashMap<>(eventMap);

            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.CONVERSATION_ASSIGNED)
                    .eventId(conversationId)
                    .conversationId(conversationId)
                    .timestamp(LocalDateTime.now())
                    .payload(payload)
                    .senderId("SYSTEM")
                    .senderType("SYSTEM")
                    .build();

            // Notify agent
            if (agentId != null) messageSender.sendToUser(agentId, wsMessage);
            // Notify customer
            if (customerId != null) messageSender.sendToUser(customerId, wsMessage);

            log.debug("Notified about conversation assignment - agentId: {}, conversationId: {}", agentId, conversationId);

        } catch (Exception e) {
            log.error("Error processing ConversationAssignedEvent", e);
        }
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
