package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.enums.ConversationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversationEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChatConversationEntity c WHERE c.conversationId = :id")
    Optional<ChatConversationEntity> lockById(@Param("id") String conversationId);

    List<ChatConversationEntity> findByStatusOrderByCreatedAtAsc(ConversationStatus status);

    List<ChatConversationEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<ChatConversationEntity> findByAssignedAgentAndStatusIn(String assignedAgent, List<ConversationStatus> statuses);

    List<ChatConversationEntity> findByAssignedAgentOrderByCreatedAtDesc(String assignedAgent);

    @Modifying
    @Query("""
        UPDATE ChatConversationEntity c
        SET c.assignedAgent = :agentId, c.status = :inProgress
        WHERE c.conversationId = :conversationId
          AND c.status = :newStatus
          AND c.assignedAgent IS NULL
        """)
    int assignAgentIfUnassigned(@Param("conversationId") String conversationId,
                                @Param("agentId") String agentId,
                                @Param("inProgress") ConversationStatus inProgress,
                                @Param("newStatus") ConversationStatus newStatus);

    // Phase 2 queries
    long countByStatus(ConversationStatus status);

    List<ChatConversationEntity> findByAssignedAgentIsNullAndStatus(ConversationStatus status);

    List<ChatConversationEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<ChatConversationEntity> findByAssignedAgentAndCreatedAtBetween(String agentId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT c FROM ChatConversationEntity c WHERE c.slaDeadline < :now AND c.firstResponseAt IS NULL")
    List<ChatConversationEntity> findSlaViolations(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM ChatConversationEntity c WHERE c.slaDeadline > :now AND c.slaDeadline < :threshold AND c.firstResponseAt IS NULL")
    List<ChatConversationEntity> findSlaAtRisk(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(c) FROM ChatConversationEntity c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    long countCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByStatusAndCreatedAtBefore(ConversationStatus status, LocalDateTime createdBefore);

    List<ChatConversationEntity> findTop10ByStatusOrderByCreatedAtAsc(ConversationStatus status);
}