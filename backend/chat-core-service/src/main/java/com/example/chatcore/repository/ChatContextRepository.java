package com.example.chatcore.repository;

import com.example.chatcore.entity.ChatContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatContextRepository extends JpaRepository<ChatContextEntity, String> {
}
