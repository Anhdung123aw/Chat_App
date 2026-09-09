package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatAgentEntity;
import com.example.chatcore.enums.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho ChatAgentEntity
 */
@Repository
public interface ChatAgentRepository extends JpaRepository<ChatAgentEntity, String> {

    /**
     * Tìm agents theo status
     */
    List<ChatAgentEntity> findByStatus(AgentStatus status);

    /**
     * Tìm agents online và có thể nhận thêm chat
     */
    @Query("SELECT a FROM ChatAgentEntity a WHERE a.status = 'ONLINE' AND a.currentChatCount < a.maxConcurrentChats")
    List<ChatAgentEntity> findAvailableAgents();

    /**
     * Tìm agents theo skill (JSON contains)
     * Oracle: JSON_EXISTS hoặc LIKE pattern
     */
    @Query(value = "SELECT * FROM CHAT_AGENT WHERE STATUS = 'ONLINE' " +
            "AND CURRENT_CHAT_COUNT < MAX_CONCURRENT_CHATS " +
            "AND (SKILLS IS NULL OR SKILLS LIKE '%' || :skill || '%') " +
            "ORDER BY CURRENT_CHAT_COUNT ASC", nativeQuery = true)
    List<ChatAgentEntity> findAvailableAgentsBySkill(@Param("skill") String skill);

    /**
     * Đếm số agent online
     */
    long countByStatus(AgentStatus status);

    /**
     * Tìm agents không hoạt động từ một thời điểm
     */
    List<ChatAgentEntity> findByLastActiveAtBefore(LocalDateTime dateTime);

    /**
     * Check agent tồn tại và online
     */
    boolean existsByAgentIdAndStatus(String agentId, AgentStatus status);
}
