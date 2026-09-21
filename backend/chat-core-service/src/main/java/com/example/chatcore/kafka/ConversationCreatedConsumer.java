package com.example.chatcore.kafka;

import com.example.chatcore.service.AutoAssignmentService;
import com.example.chatcore.service.SlaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Kafka Consumer lắng nghe event chat.conversation.created
 * Tự động assign conversation cho agent phù hợp
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class    ConversationCreatedConsumer {

    private final AutoAssignmentService autoAssignmentService;
    private final SlaService slaService;
    private final ObjectMapper objectMapper;

    /**
     * Consume event chat.conversation.created
     * Trigger auto assignment cho conversation mới
     */
    @KafkaListener(
            topics = "chat.conversation.created",
            groupId = "chat-core-auto-assignment",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeConversationCreated(String message) {
        try {
            log.info("Received conversation.created event: {}", message);

            JsonNode event = objectMapper.readTree(message);
            String conversationId = event.get("conversationId").asText();
            String topicCode = event.has("topicCode") ? event.get("topicCode").asText() : null;

            log.info("Processing auto assignment for conversation: {} (topic: {})", conversationId, topicCode);

            // Auto assign conversation
            boolean assigned = autoAssignmentService.autoAssignConversation(conversationId);

            if (assigned) {
                log.info(" Successfully auto-assigned conversation: {}", conversationId);
            } else {
                log.warn(" Failed to auto-assign conversation: {} (no available agent)", conversationId);
            }

        } catch (Exception e) {
            log.error(" Error processing conversation.created event: {}", e.getMessage(), e);
            // Don't throw - continue processing other messages
        }
    }
}
