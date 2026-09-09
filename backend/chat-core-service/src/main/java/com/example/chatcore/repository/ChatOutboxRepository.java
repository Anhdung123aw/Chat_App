package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatOutboxEntity;
import com.example.chatcore.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatOutboxRepository extends JpaRepository<ChatOutboxEntity, String> {

    List<ChatOutboxEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}