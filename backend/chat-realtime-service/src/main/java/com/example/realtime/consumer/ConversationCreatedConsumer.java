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
 * Kafka Consumer for Conversation Created Events
 * Listens to topic: chat.conversation.created
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationCreatedConsumer {

    private final WebSocketMessageSender messageSender;

    @KafkaListener(
            topics = "${chat.realtime.kafka.topics.conversation-created:chat.conversation.created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Map<String, Object> eventMap) {
        try {
            String conversationId = str(eventMap, "conversationId");
            log.info("Received ConversationCreatedEvent - conversationId: {}", conversationId);

            Map<String, Object> payload = new HashMap<>(eventMap);

            WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type(WebSocketMessage.MessageType.CONVERSATION_STATUS_CHANGED)
                    .eventId(conversationId + "-CREATED")
                    .conversationId(conversationId)
                    .timestamp(LocalDateTime.now())
                    .payload(payload)
                    .senderId("SYSTEM")
                    .senderType("SYSTEM")
                    .build();

            // Broadcast to all connected clients (especially agents) so they can update queue
            messageSender.broadcast(wsMessage);

        } catch (Exception e) {
            log.error("Error processing ConversationCreatedEvent", e);
        }
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
