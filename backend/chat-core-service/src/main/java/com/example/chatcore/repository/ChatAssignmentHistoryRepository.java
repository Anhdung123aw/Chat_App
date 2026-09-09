package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatAssignmentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatAssignmentHistoryRepository extends JpaRepository<ChatAssignmentHistoryEntity, String> {
    
    List<ChatAssignmentHistoryEntity> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}
