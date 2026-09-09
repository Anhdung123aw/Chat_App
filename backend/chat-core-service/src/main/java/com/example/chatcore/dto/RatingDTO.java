package com.example.chatcore.dto;

import com.example.chatcore.entity.ChatRatingEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RatingDTO {

    /**
     * Request: Submit rating (CSAT)
     */
    public record SubmitRatingRequest(
            @NotNull(message = "Rating không được null")
            @Min(value = 1, message = "Rating phải từ 1-5")
            @Max(value = 5, message = "Rating phải từ 1-5")
            Integer rating,
            String comment
    ) {}

    /**
     * Response: Rating info
     */
    public record RatingResponse(
            String ratingId,
            String conversationId,
            Integer rating,
            String comment,
            LocalDateTime createdAt
    ) {
        public static RatingResponse from(ChatRatingEntity entity) {
            return new RatingResponse(
                    entity.getRatingId(),
                    entity.getConversationId(),
                    entity.getRating(),
                    entity.getComment(),
                    entity.getCreatedAt()
            );
        }
    }

    /**
     * Response: CSAT score tổng hợp
     */
    public record CsatScoreResponse(
            Double averageRating,
            Double csatPercentage,
            Long totalRatings,
            RatingDistribution distribution
    ) {}

    /**
     * Phân bố đánh giá theo từng điểm
     */
    public record RatingDistribution(
            Long oneStar,
            Long twoStar,
            Long threeStar,
            Long fourStar,
            Long fiveStar
    ) {}

    /**
     * Response: CSAT theo khoảng thời gian
     */
    public record CsatReportResponse(
            Double averageRating,
            Double csatPercentage,
            Long totalRatings,
            LocalDateTime startDate,
            LocalDateTime endDate,
            RatingDistribution distribution
    ) {}
}
