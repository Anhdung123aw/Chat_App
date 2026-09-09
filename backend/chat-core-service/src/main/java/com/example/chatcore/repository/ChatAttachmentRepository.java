package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatAttachmentRepository extends JpaRepository<ChatAttachmentEntity, String> {
    
    List<ChatAttachmentEntity> findByMessageId(String messageId);
    
    List<ChatAttachmentEntity> findByConversationId(String conversationId);
}
