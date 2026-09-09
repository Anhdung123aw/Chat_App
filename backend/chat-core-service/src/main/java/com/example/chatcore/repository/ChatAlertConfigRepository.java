package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatAlertConfigEntity;
import com.example.chatcore.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ChatAlertConfigEntity
 */
@Repository
public interface ChatAlertConfigRepository extends JpaRepository<ChatAlertConfigEntity, String> {

    /**
     * Tìm alert config theo loại
     */
    Optional<ChatAlertConfigEntity> findByAlertType(AlertType alertType);

    /**
     * Tìm tất cả configs đang enabled
     */
    List<ChatAlertConfigEntity> findByEnabled(Boolean enabled);

    /**
     * Tìm configs theo loại và enabled
     */
    Optional<ChatAlertConfigEntity> findByAlertTypeAndEnabled(AlertType alertType, Boolean enabled);

    /**
     * Check alert type đã có config chưa
     */
    boolean existsByAlertType(AlertType alertType);
}
