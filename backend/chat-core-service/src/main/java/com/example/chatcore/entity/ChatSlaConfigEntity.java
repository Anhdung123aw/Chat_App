package com.example.chatcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * Entity cho bảng CHAT_SLA_CONFIG
 * Cấu hình SLA (Service Level Agreement) theo từng chủ đề chat
 */
@Entity
@Table(name = "CHAT_SLA_CONFIG", indexes = {
        @Index(name = "IDX_SLA_TOPIC", columnList = "topicCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSlaConfigEntity {

    @Id
    @Column(name = "CONFIG_ID", length = 50)
    private String configId;

    @Column(name = "TOPIC_CODE", length = 50, nullable = false, unique = true)
    private String topicCode;

    @Column(name = "FIRST_RESPONSE_TIME_SECONDS", nullable = false)
    private Integer firstResponseTimeSeconds;

    @Column(name = "RESOLUTION_TIME_SECONDS")
    private Integer resolutionTimeSeconds;

    @Column(name = "ENABLED", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UPDATED_AT", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
