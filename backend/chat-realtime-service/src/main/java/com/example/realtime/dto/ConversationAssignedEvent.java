package com.example.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a conversation is assigned to an agent
 * Consumed from Kafka topic: chat.conversation.assigned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationAssignedEvent {
    private String conversationId;
    private String customerId;
    private String agentId;
    private String topicCode;
    private String assignType; // AUTO, MANUAL, HANDOVER
    private LocalDateTime assignedAt;
}
