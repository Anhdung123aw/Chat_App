package com.example.chatcore.dto;

import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.enums.ConversationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class ConversationDTO {

    public record CreateConversationRequest(
            @NotBlank(message = "User ID is required") String userId,
            String merchantId,
            String topicCode,
            @Valid ContextDTO.ChatContextRequest context  // Context capture theo section 6
    ) {}

    public record ConversationResponse(
            String conversationId,
            String userId,
            String merchantId,
            String topicCode,
            ConversationStatus status,
            String assignedAgent,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ConversationResponse from(ChatConversationEntity c) {
            return new ConversationResponse(
                    c.getConversationId(), c.getUserId(), c.getMerchantId(), c.getTopicCode(),
                    c.getStatus(), c.getAssignedAgent(), c.getCreatedAt(), c.getUpdatedAt());
        }
    }

    public record AssignRequest(@NotBlank(message = "Agent ID is required") String agentId) {}

    public record TransferRequest(
            @NotBlank(message = "Target agent ID is required") String toAgentId,
            String transferredBy,
            String reason
    ) {}
}