package com.example.chatcore.entity;

import com.example.chatcore.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT_OUTBOX")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatOutboxEntity {

    @Id
    @Column(name = "EVENT_ID", length = 36)
    private String eventId;

    @Column(name = "AGGREGATE_ID", nullable = false, length = 36)
    private String aggregateId;

    @Column(name = "EVENT_TYPE", nullable = false, length = 128)
    private String eventType;

    @Column(name = "PAYLOAD", nullable = false, columnDefinition = "CLOB")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 16)
    private OutboxStatus status;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "PUBLISHED_AT")
    private LocalDateTime publishedAt;

    public static ChatOutboxEntity create(String eventId, String aggregateId, String eventType, String payloadJson) {
        ChatOutboxEntity o = new ChatOutboxEntity();
        o.eventId = eventId;
        o.aggregateId = aggregateId;
        o.eventType = eventType;
        o.payload = payloadJson;
        o.status = OutboxStatus.PENDING;
        o.createdAt = LocalDateTime.now();
        return o;
    }
}