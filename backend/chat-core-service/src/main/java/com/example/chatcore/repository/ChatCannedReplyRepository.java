package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatCannedReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ChatCannedReplyEntity
 */
@Repository
public interface ChatCannedReplyRepository extends JpaRepository<ChatCannedReplyEntity, String> {

    /**
     * Tìm canned replies theo category
     */
    List<ChatCannedReplyEntity> findByCategory(String category);

    /**
     * Tìm canned reply theo shortcuts
     */
    Optional<ChatCannedReplyEntity> findByShortcuts(String shortcuts);

    /**
     * Tìm canned replies theo locale
     */
    List<ChatCannedReplyEntity> findByLocale(String locale);

    /**
     * Tìm canned replies theo category và locale
     */
    List<ChatCannedReplyEntity> findByCategoryAndLocale(String category, String locale);

    /**
     * Tìm canned replies do agent tạo
     */
    List<ChatCannedReplyEntity> findByCreatedBy(String createdBy);
}
