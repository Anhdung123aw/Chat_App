package com.example.chatcore.service;

import com.example.chatcore.dto.CannedReplyDTO.*;
import com.example.chatcore.entity.ChatCannedReplyEntity;
import com.example.chatcore.repository.ChatCannedReplyRepository;
import com.example.chatcore.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service quản lý Canned Reply (Template trả lời nhanh)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CannedReplyService {

    private final ChatCannedReplyRepository cannedReplyRepository;

    /**
     * Tạo canned reply mới
     */
    @Transactional
    public CannedReplyResponse createCannedReply(CreateCannedReplyRequest request) {
        log.info("Creating canned reply: {}", request.title());

        ChatCannedReplyEntity entity = ChatCannedReplyEntity.builder()
                .replyId(UUID.randomUUID().toString())
                .category(request.category())
                .title(request.title())
                .content(request.content())
                .shortcuts(request.shortcuts())
                .locale(request.locale() != null ? request.locale() : "vi")
                .createdBy(request.createdBy())
                .build();

        cannedReplyRepository.save(entity);
        log.info("Created canned reply: {}", entity.getReplyId());

        return CannedReplyResponse.from(entity);
    }

    /**
     * Update canned reply
     */
    @Transactional
    public CannedReplyResponse updateCannedReply(String replyId, UpdateCannedReplyRequest request) {
        log.info("Updating canned reply: {}", replyId);

        ChatCannedReplyEntity entity = cannedReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("Canned reply not found: " + replyId));

        if (request.category() != null) {
            entity.setCategory(request.category());
        }
        if (request.title() != null) {
            entity.setTitle(request.title());
        }
        if (request.content() != null) {
            entity.setContent(request.content());
        }
        if (request.shortcuts() != null) {
            entity.setShortcuts(request.shortcuts());
        }
        if (request.locale() != null) {
            entity.setLocale(request.locale());
        }

        cannedReplyRepository.save(entity);
        return CannedReplyResponse.from(entity);
    }

    /**
     * Xóa canned reply
     */
    @Transactional
    public void deleteCannedReply(String replyId) {
        log.info("Deleting canned reply: {}", replyId);
        cannedReplyRepository.deleteById(replyId);
    }

    /**
     * Get canned reply by ID
     */
    public CannedReplyResponse getCannedReplyById(String replyId) {
        ChatCannedReplyEntity entity = cannedReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("Canned reply not found: " + replyId));
        return CannedReplyResponse.from(entity);
    }

    /**
     * List canned replies theo category
     */
    public CannedReplyListResponse listByCategory(String category, String locale) {
        List<ChatCannedReplyEntity> entities;
        
        if (category != null && locale != null) {
            entities = cannedReplyRepository.findByCategoryAndLocale(category, locale);
        } else if (category != null) {
            entities = cannedReplyRepository.findByCategory(category);
        } else if (locale != null) {
            entities = cannedReplyRepository.findByLocale(locale);
        } else {
            entities = cannedReplyRepository.findAll();
        }

        List<CannedReplyResponse> responses = entities.stream()
                .map(CannedReplyResponse::from)
                .toList();

        return new CannedReplyListResponse(responses, responses.size());
    }

    /**
     * Tìm canned reply theo shortcuts
     */
    public CannedReplyResponse findByShortcuts(String shortcuts) {
        ChatCannedReplyEntity entity = cannedReplyRepository.findByShortcuts(shortcuts)
                .orElseThrow(() -> new ResourceNotFoundException("Canned reply not found for shortcuts: " + shortcuts));
        return CannedReplyResponse.from(entity);
    }
}
