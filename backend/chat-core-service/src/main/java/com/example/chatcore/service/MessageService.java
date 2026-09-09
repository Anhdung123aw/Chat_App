package com.example.chatcore.service;

import com.example.chatcore.entity.ChatMessageEntity;
import com.example.chatcore.entity.ChatOutboxEntity;
import com.example.chatcore.repository.ChatMessageRepository;
import com.example.chatcore.repository.ChatOutboxRepository;
import com.example.chatcore.exception.ResourceNotFoundException;
import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.repository.ChatConversationRepository;
import com.example.chatcore.enums.SenderType;
import com.example.chatcore.enums.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatConversationRepository conversationRepository;
    private final ChatOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatMessageEntity sendMessage(String conversationId, SenderType senderType, String senderId,
                                   MessageType messageType, String content, String clientMessageId) {

        var existing = messageRepository.findByConversationIdAndClientMessageId(conversationId, clientMessageId);
        if (existing.isPresent()) {
            return existing.get();
        }

        ChatConversationEntity conversation = conversationRepository.lockById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        long nextSeq = messageRepository.findMaxSeqNo(conversationId) + 1;

        ChatMessageEntity message = ChatMessageEntity.create(
                UUID.randomUUID().toString(), conversationId, senderType, senderId,
                messageType, content, clientMessageId, nextSeq);
        messageRepository.save(message);

        conversation.setLastMessageId(message.getMessageId());
        if (senderType == SenderType.AGENT && conversation.getFirstResponseAt() == null) {
            conversation.setFirstResponseAt(LocalDateTime.now());
        }

        publishEvent(message);

        return message;
    }

    public List<ChatMessageEntity> getHistory(String conversationId) {
        return messageRepository.findByConversationIdOrderBySeqNoAsc(conversationId);
    }

    public List<ChatMessageEntity> syncAfter(String conversationId, long afterSequence) {
        return messageRepository.findByConversationIdAndSeqNoGreaterThanOrderBySeqNoAsc(conversationId, afterSequence);
    }

    @Transactional
    public void markAsRead(String conversationId, String messageId) {
        int updated = messageRepository.markAsRead(conversationId, messageId, LocalDateTime.now());
        if (updated > 0) {
            publishEvent(Map.of(
                    "eventType", "MESSAGE_READ",
                    "conversationId", conversationId,
                    "messageId", messageId,
                    "timestamp", LocalDateTime.now().toString()
            ), "chat.message.read");
        }
    }

    private void publishEvent(ChatMessageEntity message) {
        Map<String, Object> payload = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "type", "MESSAGE",
                "conversationId", message.getConversationId(),
                "messageId", message.getMessageId(),
                "sequence", message.getSeqNo(),
                "senderType", message.getSenderType().name(),
                "senderId", message.getSenderId(),
                "messageType", message.getMessageType().name(),
                "content", message.getContent() == null ? "" : message.getContent(),
                "timestamp", message.getCreatedAt().toString()
        );
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxRepository.save(ChatOutboxEntity.create(
                    UUID.randomUUID().toString(), message.getConversationId(), "chat.message.created", json));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize message event", e);
        }
    }

    private void publishEvent(Map<String, Object> payload, String eventType) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            String aggregateId = (String) payload.get("conversationId");
            outboxRepository.save(ChatOutboxEntity.create(UUID.randomUUID().toString(), aggregateId, eventType, json));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}