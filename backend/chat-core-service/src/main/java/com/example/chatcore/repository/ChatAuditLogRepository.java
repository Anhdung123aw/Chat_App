package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatAuditLogRepository extends JpaRepository<ChatAuditLogEntity, String> {
    
    List<ChatAuditLogEntity> findByConversationIdOrderByCreatedAtDesc(String conversationId);
    
    List<ChatAuditLogEntity> findByActorIdOrderByCreatedAtDesc(String actorId);
}
