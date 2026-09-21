package com.example.chatcore.kafka;

import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.enums.SenderType;
import com.example.chatcore.repository.ChatConversationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Kafka Consumer lắng nghe event chat.message.created
 * Update FIRST_RESPONSE_AT khi agent reply lần đầu (cho SLA tracking)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageCreatedConsumer {

    private final ChatConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consume event chat.message.created
     * Track first response time để tính SLA
     */
    @KafkaListener(
            topics = "chat.message.created",
            groupId = "chat-core-sla-tracking",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeMessageCreated(String message) {
        try {
            log.debug("Received message.created event: {}", message);

            JsonNode event = objectMapper.readTree(message);
            String conversationId = event.get("conversationId").asText();
            String senderType = event.get("senderType").asText();

            // Only process if message is from AGENT
            if (!"AGENT".equals(senderType)) {
                log.debug("Message from {}, skipping SLA tracking", senderType);
                return;
            }

            // Update first response time if not set yet
            ChatConversationEntity conversation = conversationRepository.findById(conversationId)
                    .orElse(null);

            if (conversation == null) {
                log.warn("Conversation not found: {}", conversationId);
                return;
            }

            if (conversation.getFirstResponseAt() == null) {
                conversation.setFirstResponseAt(LocalDateTime.now());
                conversationRepository.save(conversation);
                log.info(" Tracked first response for conversation: {}", conversationId);
            }

        } catch (Exception e) {
            log.error(" Error processing message.created event: {}", e.getMessage(), e);
            // Don't throw - continue processing other messages
        }
    }
}
