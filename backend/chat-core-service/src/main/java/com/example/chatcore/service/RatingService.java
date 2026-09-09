package com.example.chatcore.service;

import com.example.chatcore.dto.RatingDTO.*;
import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.entity.ChatRatingEntity;
import com.example.chatcore.enums.ConversationStatus;
import com.example.chatcore.exception.InvalidRatingException;
import com.example.chatcore.repository.ChatConversationRepository;
import com.example.chatcore.repository.ChatRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service quản lý Rating/CSAT
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final ChatRatingRepository ratingRepository;
    private final ChatConversationRepository conversationRepository;

    /**
     * Submit rating cho conversation
     */
    @Transactional
    public RatingResponse submitRating(String conversationId, SubmitRatingRequest request) {
        log.info("Submitting rating for conversation: {}, rating: {}", conversationId, request.rating());

        // Check conversation đã closed chưa
        ChatConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new InvalidRatingException("Conversation not found: " + conversationId));

        if (conversation.getStatus() != ConversationStatus.CLOSED) {
            throw InvalidRatingException.conversationNotClosed(conversationId);
        }

        // Check đã rate chưa
        if (ratingRepository.existsByConversationId(conversationId)) {
            throw InvalidRatingException.alreadyRated(conversationId);
        }

        // Tạo rating
        ChatRatingEntity entity = ChatRatingEntity.builder()
                .ratingId(UUID.randomUUID().toString())
                .conversationId(conversationId)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        ratingRepository.save(entity);
        log.info("Rating submitted: {}", entity.getRatingId());

        return RatingResponse.from(entity);
    }

    /**
     * Get rating của conversation
     */
    public RatingResponse getRatingByConversation(String conversationId) {
        ChatRatingEntity entity = ratingRepository.findByConversationId(conversationId)
                .orElse(null);
        
        return entity != null ? RatingResponse.from(entity) : null;
    }

    /**
     * Tính CSAT score tổng hợp
     */
    public CsatScoreResponse calculateCsatScore() {
        Double avgRating = ratingRepository.calculateAverageCsat();
        Long totalRatings = ratingRepository.count();

        if (avgRating == null || totalRatings == 0) {
            return new CsatScoreResponse(0.0, 0.0, 0L, new RatingDistribution(0L, 0L, 0L, 0L, 0L));
        }

        // CSAT percentage = avgRating * 20 (convert 1-5 scale to 0-100%)
        Double csatPercentage = avgRating * 20.0;

        // Tính distribution
        RatingDistribution distribution = new RatingDistribution(
                ratingRepository.countByRating(1),
                ratingRepository.countByRating(2),
                ratingRepository.countByRating(3),
                ratingRepository.countByRating(4),
                ratingRepository.countByRating(5)
        );

        return new CsatScoreResponse(avgRating, csatPercentage, totalRatings, distribution);
    }

    /**
     * Tính CSAT trong khoảng thời gian
     */
    public CsatReportResponse calculateCsatReport(LocalDateTime startDate, LocalDateTime endDate) {
        Double avgRating = ratingRepository.calculateAverageCsatBetween(startDate, endDate);
        Long totalRatings = ratingRepository.countRatingsBetween(startDate, endDate);

        if (avgRating == null || totalRatings == 0) {
            return new CsatReportResponse(0.0, 0.0, 0L, startDate, endDate, 
                    new RatingDistribution(0L, 0L, 0L, 0L, 0L));
        }

        Double csatPercentage = avgRating * 20.0;

        // Distribution trong khoảng thời gian (simplified - count all)
        RatingDistribution distribution = new RatingDistribution(
                ratingRepository.countByRating(1),
                ratingRepository.countByRating(2),
                ratingRepository.countByRating(3),
                ratingRepository.countByRating(4),
                ratingRepository.countByRating(5)
        );

        return new CsatReportResponse(avgRating, csatPercentage, totalRatings, 
                startDate, endDate, distribution);
    }
}
