package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, String> {

    Optional<ChatMessageEntity> findByConversationIdAndClientMessageId(String conversationId, String clientMessageId);

    List<ChatMessageEntity> findByConversationIdAndSeqNoGreaterThanOrderBySeqNoAsc(String conversationId, Long afterSequence);

    List<ChatMessageEntity> findByConversationIdOrderBySeqNoAsc(String conversationId);

    Long countByConversationId(String conversationId);

    @Query("SELECT COALESCE(MAX(m.seqNo), 0) FROM ChatMessageEntity m WHERE m.conversationId = :conversationId")
    Long findMaxSeqNo(String conversationId);

    @Modifying
    @Query("UPDATE ChatMessageEntity m SET m.readAt = :readAt WHERE m.messageId = :messageId AND m.conversationId = :conversationId AND m.readAt IS NULL")
    int markAsRead(String conversationId, String messageId, LocalDateTime readAt);
}