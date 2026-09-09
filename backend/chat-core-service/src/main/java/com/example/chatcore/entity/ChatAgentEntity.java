package com.example.chatcore.entity;

import com.example.chatcore.enums.AgentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * Entity cho bảng CHAT_AGENT
 * Quản lý thông tin CS Agent: trạng thái, kỹ năng, capacity
 */
@Entity
@Table(name = "CHAT_AGENT", indexes = {
        @Index(name = "IDX_AGENT_STATUS", columnList = "status"),
        @Index(name = "IDX_AGENT_LAST_ACTIVE", columnList = "lastActiveAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAgentEntity {

    @Id
    @Column(name = "AGENT_ID", length = 50)
    @Comment("ID duy nhất của agent")
    private String agentId;

    @Column(name = "AGENT_NAME", length = 100, nullable = false)
    @Comment("Tên hiển thị của agent")
    private String agentName;

    @Column(name = "EMAIL", length = 100)
    @Comment("Email của agent")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, nullable = false)
    @Comment("Trạng thái agent: ONLINE, OFFLINE, BUSY, AWAY")
    private AgentStatus status;

    @Column(name = "SKILLS", length = 500)
    @Comment("Danh sách kỹ năng/topic agent xử lý (JSON array)")
    private String skills; // JSON: ["ORDER_SUPPORT", "PAYMENT"]

    @Column(name = "MAX_CONCURRENT_CHATS", nullable = false)
    @Builder.Default
    @Comment("Số lượng chat tối đa agent có thể xử lý cùng lúc")
    private Integer maxConcurrentChats = 5;

    @Column(name = "CURRENT_CHAT_COUNT", nullable = false)
    @Builder.Default
    @Comment("Số lượng chat hiện tại agent đang xử lý")
    private Integer currentChatCount = 0;

    @Column(name = "LAST_ACTIVE_AT")
    @Comment("Thời điểm agent hoạt động lần cuối")
    private LocalDateTime lastActiveAt;

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

    /**
     * Kiểm tra agent có thể nhận thêm chat không
     */
    public boolean canAcceptMoreChats() {
        return status == AgentStatus.ONLINE && currentChatCount < maxConcurrentChats;
    }

    /**
     * Tăng số lượng chat hiện tại
     */
    public void incrementChatCount() {
        this.currentChatCount++;
    }

    /**
     * Giảm số lượng chat hiện tại
     */
    public void decrementChatCount() {
        if (this.currentChatCount > 0) {
            this.currentChatCount--;
        }
    }
}
