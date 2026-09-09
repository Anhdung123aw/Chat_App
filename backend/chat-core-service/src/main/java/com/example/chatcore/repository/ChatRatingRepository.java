package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho ChatRatingEntity
 */
@Repository
public interface ChatRatingRepository extends JpaRepository<ChatRatingEntity, String> {

    /**
     * Tìm rating theo conversation ID
     */
    Optional<ChatRatingEntity> findByConversationId(String conversationId);

    /**
     * Check conversation đã được rate chưa
     */
    boolean existsByConversationId(String conversationId);

    /**
     * Tìm ratings trong khoảng thời gian
     */
    List<ChatRatingEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Tính điểm CSAT trung bình
     */
    @Query("SELECT AVG(r.rating) FROM ChatRatingEntity r")
    Double calculateAverageCsat();

    /**
     * Tính điểm CSAT trung bình trong khoảng thời gian
     */
    @Query("SELECT AVG(r.rating) FROM ChatRatingEntity r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    Double calculateAverageCsatBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm số lượng ratings theo điểm
     */
    long countByRating(Integer rating);

    /**
     * Đếm tổng số ratings
     */
    @Query("SELECT COUNT(r) FROM ChatRatingEntity r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    long countRatingsBetween(@Param("startDate") LocalDateTime startDate, 
                            @Param("endDate") LocalDateTime endDate);

    /**
     * Tính CSAT trung bình cho danh sách conversations
     */
    @Query("SELECT AVG(r.rating) FROM ChatRatingEntity r WHERE r.conversationId IN :conversationIds")
    Double calculateAverageCsatForConversations(@Param("conversationIds") List<String> conversationIds);
}
