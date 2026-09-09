package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatSlaConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ChatSlaConfigEntity
 */
@Repository
public interface ChatSlaConfigRepository extends JpaRepository<ChatSlaConfigEntity, String> {

    /**
     * Tìm SLA config theo topic code
     */
    Optional<ChatSlaConfigEntity> findByTopicCode(String topicCode);

    /**
     * Tìm tất cả configs đang enabled
     */
    List<ChatSlaConfigEntity> findByEnabled(Boolean enabled);

    /**
     * Check topic code đã có config chưa
     */
    boolean existsByTopicCode(String topicCode);
}
