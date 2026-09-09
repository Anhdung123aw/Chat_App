package com.example.chatcore.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_ASSIGNMENT_HISTORY")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatAssignmentHistoryEntity {

    @Id
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "CONVERSATION_ID", nullable = false, length = 36)
    private String conversationId;

    @Column(name = "FROM_AGENT", length = 64)
    private String fromAgent;

    @Column(name = "TO_AGENT", length = 64)
    private String toAgent;

    @Column(name = "ASSIGN_TYPE", length = 16)
    private String assignType; // AUTO / MANUAL / HANDOVER

    @Column(name = "ASSIGNED_BY", length = 64)
    private String assignedBy;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ChatAssignmentHistoryEntity create(String id, String conversationId,
                                               String fromAgent, String toAgent,
                                               String assignType, String assignedBy) {
        ChatAssignmentHistoryEntity h = new ChatAssignmentHistoryEntity();
        h.id = id;
        h.conversationId = conversationId;
        h.fromAgent = fromAgent;
        h.toAgent = toAgent;
        h.assignType = assignType;
        h.assignedBy = assignedBy;
        h.createdAt = LocalDateTime.now();
        return h;
    }
}
