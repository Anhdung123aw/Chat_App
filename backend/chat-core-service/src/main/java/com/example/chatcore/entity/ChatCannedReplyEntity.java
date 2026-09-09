package com.example.chatcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * Entity cho bảng CHAT_CANNED_REPLY
 * Template trả lời nhanh cho agent
 */
@Entity
@Table(name = "CHAT_CANNED_REPLY", indexes = {
        @Index(name = "IDX_CANNED_CATEGORY", columnList = "category"),
        @Index(name = "IDX_CANNED_SHORTCUTS", columnList = "shortcuts")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCannedReplyEntity {

    @Id
    @Column(name = "REPLY_ID", length = 50)
    @Comment("ID duy nhất của template")
    private String replyId;

    @Column(name = "CATEGORY", length = 50, nullable = false)
    @Comment("Danh mục: GREETING, CLOSING, FAQ, TROUBLESHOOTING")
    private String category;

    @Column(name = "TITLE", length = 200, nullable = false)
    @Comment("Tiêu đề template")
    private String title;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    @Comment("Nội dung template, có thể chứa placeholder {agentName}, {userName}")
    private String content;

    @Column(name = "SHORTCUTS", length = 50)
    @Comment("Phím tắt để gọi nhanh: /hello, /thanks, /bye")
    private String shortcuts;

    @Column(name = "LOCALE", length = 10, nullable = false)
    @Builder.Default
    @Comment("Ngôn ngữ: vi, en")
    private String locale = "vi";

    @Column(name = "CREATED_BY", length = 50)
    @Comment("Agent/Admin tạo template")
    private String createdBy;

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
