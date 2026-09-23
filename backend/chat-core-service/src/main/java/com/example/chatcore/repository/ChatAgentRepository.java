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

@Repository
public interface ChatAgentRepository extends JpaRepository<ChatAgentEntity, String> {
    List<ChatAgentEntity> findByStatus(AgentStatus status);
    @Query("SELECT a FROM ChatAgentEntity a WHERE a.status = 'ONLINE' AND a.currentChatCount < a.maxConcurrentChats")
    List<ChatAgentEntity> findAvailableAgents();
    @Query(value = "SELECT * FROM CHAT_AGENT WHERE STATUS = 'ONLINE' " +
            "AND CURRENT_CHAT_COUNT < MAX_CONCURRENT_CHATS " +
            "AND (SKILLS IS NULL OR SKILLS LIKE '%' || :skill || '%') " +
            "ORDER BY CURRENT_CHAT_COUNT ASC", nativeQuery = true)
    List<ChatAgentEntity> findAvailableAgentsBySkill(@Param("skill") String skill);
    long countByStatus(AgentStatus status);
    List<ChatAgentEntity> findByLastActiveAtBefore(LocalDateTime dateTime);

    boolean existsByAgentIdAndStatus(String agentId, AgentStatus status);
}
