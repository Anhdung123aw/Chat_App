package com.example.chatcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * Entity cho bảng CHAT_RATING
 * Đánh giá CSAT (Customer Satisfaction) từ khách hàng
 */
@Entity
@Table(name = "CHAT_RATING", indexes = {
        @Index(name = "UNQ_RATING_CONVERSATION", columnList = "conversationId", unique = true),
        @Index(name = "IDX_RATING_CREATED", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRatingEntity {

    @Id
    @Column(name = "RATING_ID", length = 50)
    @Comment("ID duy nhất của rating")
    private String ratingId;

    @Column(name = "CONVERSATION_ID", length = 50, nullable = false, unique = true)
    @Comment("Conversation được đánh giá (1 conversation chỉ rate 1 lần)")
    private String conversationId;

    @Column(name = "RATING", nullable = false)
    @Comment("Điểm đánh giá từ 1-5 (1: rất không hài lòng, 5: rất hài lòng)")
    private Integer rating;

    @Lob
    @Column(name = "COMMENT")
    @Comment("Nhận xét chi tiết từ khách hàng")
    private String comment;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Validate rating phải từ 1-5
     */
    @PrePersist
    @PreUpdate
    protected void validateRating() {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5");
        }
    }
}
