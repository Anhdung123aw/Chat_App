package com.example.chatcore.entity;

import com.example.chatcore.enums.AlertType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * Entity cho bảng CHAT_ALERT_CONFIG
 * Cấu hình cảnh báo cho hệ thống vận hành
 */
@Entity
@Table(name = "CHAT_ALERT_CONFIG", indexes = {
        @Index(name = "IDX_ALERT_TYPE", columnList = "alertType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAlertConfigEntity {

    @Id
    @Column(name = "ALERT_ID", length = 50)
    private String alertId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ALERT_TYPE", length = 50, nullable = false)
    private AlertType alertType;

    @Column(name = "THRESHOLD", nullable = false)
    private Integer threshold;

    @Column(name = "NOTIFICATION_CHANNELS", length = 200)
    private String notificationChannels;

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
