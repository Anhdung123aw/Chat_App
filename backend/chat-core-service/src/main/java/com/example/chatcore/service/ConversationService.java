package com.example.chatcore.service;

import com.example.chatcore.entity.ChatAssignmentHistoryEntity;
import com.example.chatcore.repository.ChatAssignmentHistoryRepository;
import com.example.chatcore.aspect.Auditable;
import com.example.chatcore.entity.ChatOutboxEntity;
import com.example.chatcore.repository.ChatOutboxRepository;
import com.example.chatcore.exception.ResourceNotFoundException;
import com.example.chatcore.exception.ConversationConflictException;
import com.example.chatcore.entity.ChatContextEntity;
import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.repository.ChatConversationRepository;
import com.example.chatcore.repository.ChatContextRepository;
import com.example.chatcore.dto.ContextDTO.ChatContextRequest;
import com.example.chatcore.dto.ConversationDTO.ConversationResponse;
import com.example.chatcore.validator.ContextValidator;
import com.example.chatcore.repository.ChatMessageRepository;
import com.example.chatcore.enums.ConversationStatus;
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
public class ConversationService {

    private final ChatConversationRepository conversationRepository;
    private final ChatContextRepository contextRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatAssignmentHistoryRepository assignmentHistoryRepository;
    private final ChatOutboxRepository outboxRepository;
    private final ContextValidator contextValidator;
    private final ObjectMapper objectMapper;
    private final SlaService slaService; // Phase 2: SLA calculation

    @Transactional
    public ChatConversationEntity createConversation(String userId, String merchantId, String topicCode, ChatContextRequest contextReq) {
        String conversationId = UUID.randomUUID().toString();
        ChatConversationEntity conversation = ChatConversationEntity.createNew(conversationId, userId, merchantId, topicCode);
        
        // Phase 2: Calculate SLA deadline based on topic
        if (topicCode != null && !topicCode.isEmpty()) {
            try {
                LocalDateTime slaDeadline = slaService.calculateSlaDeadline(topicCode, conversation.getCreatedAt());
                conversation.setSlaDeadline(slaDeadline);
            } catch (Exception e) {
                // Fallback: set default SLA deadline (1 hour) if calculation fails
                conversation.setSlaDeadline(conversation.getCreatedAt().plusHours(1));
            }
        }
        
        conversationRepository.save(conversation);

        // Save context if provided (section 6: Context Capture)
        if (contextReq != null) {
            // Validate context JSON to prevent sensitive data (section 6)
            if (contextReq.contextJson() != null) {
                contextValidator.validateContext(contextReq.contextJson());
            }

            ChatContextEntity context = ChatContextEntity.create(
                    conversationId,
                    contextReq.screenName(),
                    contextReq.feature(),
                    contextReq.lastAction(),
                    contextReq.appVersion(),
                    contextReq.os(),
                    contextReq.device(),
                    contextReq.contextJson()
            );
            contextRepository.save(context);
        }

        publishEvent(conversationId, "chat.conversation.created", Map.of(
                "conversationId", conversationId,
                "userId", userId,
                "merchantId", merchantId == null ? "" : merchantId,
                "topicCode", topicCode == null ? "" : topicCode
        ));

        return conversation;
    }

    public ChatConversationEntity getById(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
    }

    public List<ChatConversationEntity> listQueueByStatus(ConversationStatus status) {
        return conversationRepository.findByStatusOrderByCreatedAtAsc(status);
    }

    public List<ChatConversationEntity> listByUser(String userId) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    @Auditable(eventType = "CONVERSATION_ASSIGNED")
    public ChatConversationEntity assign(String conversationId, String agentId) {
        int updated = conversationRepository.assignAgentIfUnassigned(
                conversationId, agentId, ConversationStatus.IN_PROGRESS, ConversationStatus.NEW);

        if (updated == 0) {
            ChatConversationEntity existing = getById(conversationId);
            throw new ConversationConflictException(
                    "Conversation " + conversationId + " đã được agent khác nhận hoặc không ở trạng thái NEW (hiện tại: "
                            + existing.getStatus() + ", agent: " + existing.getAssignedAgent() + ")");
        }

        ChatConversationEntity conversation = getById(conversationId);

        // Log assignment history (AUTO/MANUAL type)
        ChatAssignmentHistoryEntity history = ChatAssignmentHistoryEntity.create(
                UUID.randomUUID().toString(), conversationId, null, agentId, "AUTO", "SYSTEM");
        assignmentHistoryRepository.save(history);

        publishEvent(conversationId, "chat.conversation.assigned", Map.of(
                "conversationId", conversationId,
                "agentId", agentId
        ));

        return conversation;
    }

    @Transactional
    @Auditable(eventType = "CONVERSATION_CLOSED", logOldValue = true)
    public ChatConversationEntity close(String conversationId) {
        ChatConversationEntity conversation = getById(conversationId);
        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());

        publishEvent(conversationId, "chat.conversation.closed", Map.of("conversationId", conversationId));

        return conversation;
    }

    @Transactional
    public void markMessagesAsRead(String conversationId, Long upToSequence) {
        // Mark all unread messages up to sequence as read
        messageRepository.findByConversationIdAndSeqNoGreaterThanOrderBySeqNoAsc(conversationId, 0L).stream()
                .filter(msg -> msg.getSeqNo() <= upToSequence && msg.getReadAt() == null)
                .forEach(msg -> messageRepository.markAsRead(conversationId, msg.getMessageId(), LocalDateTime.now()));
    }

    @Transactional
    @Auditable(eventType = "CONVERSATION_TRANSFERRED", logOldValue = true)
    public ChatConversationEntity transferConversation(String conversationId, String toAgentId, String transferredBy) {
        ChatConversationEntity conversation = getById(conversationId);
        String fromAgent = conversation.getAssignedAgent();

        if (fromAgent == null) {
            throw new IllegalArgumentException("Cannot transfer conversation that has no assigned agent");
        }

        if (fromAgent.equals(toAgentId)) {
            throw new IllegalArgumentException("Cannot transfer to the same agent");
        }

        // Update conversation
        conversation.setAssignedAgent(toAgentId);

        // Log handover history
        ChatAssignmentHistoryEntity history = ChatAssignmentHistoryEntity.create(
                UUID.randomUUID().toString(),
                conversationId,
                fromAgent,
                toAgentId,
                "HANDOVER",
                transferredBy != null ? transferredBy : fromAgent
        );
        assignmentHistoryRepository.save(history);

        // Publish event
        publishEvent(conversationId, "chat.conversation.transferred", Map.of(
                "conversationId", conversationId,
                "fromAgent", fromAgent,
                "toAgent", toAgentId,
                "transferredBy", transferredBy != null ? transferredBy : fromAgent
        ));

        return conversation;
    }

    private void publishEvent(String aggregateId, String eventType, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxRepository.save(ChatOutboxEntity.create(UUID.randomUUID().toString(), aggregateId, eventType, json));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }
}