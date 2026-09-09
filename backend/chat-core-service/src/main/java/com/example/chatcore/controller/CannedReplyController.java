package com.example.chatcore.controller;

import com.example.chatcore.dto.CannedReplyDTO.*;
import com.example.chatcore.service.CannedReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho Canned Reply Management
 * APIs quản lý template trả lời nhanh
 */
@RestController
@RequestMapping("/api/v1/canned-replies")
@RequiredArgsConstructor
@Slf4j
public class CannedReplyController {

    private final CannedReplyService cannedReplyService;

    /**
     * POST /api/v1/canned-replies - Tạo canned reply mới
     */
    @PostMapping
    public ResponseEntity<CannedReplyResponse> createCannedReply(@Valid @RequestBody CreateCannedReplyRequest request) {
        log.info("Creating canned reply: {}", request.title());
        CannedReplyResponse response = cannedReplyService.createCannedReply(request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/canned-replies/{replyId} - Update canned reply
     */
    @PutMapping("/{replyId}")
    public ResponseEntity<CannedReplyResponse> updateCannedReply(
            @PathVariable String replyId,
            @Valid @RequestBody UpdateCannedReplyRequest request) {
        log.info("Updating canned reply: {}", replyId);
        CannedReplyResponse response = cannedReplyService.updateCannedReply(replyId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/canned-replies/{replyId} - Delete canned reply
     */
    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteCannedReply(@PathVariable String replyId) {
        log.info("Deleting canned reply: {}", replyId);
        cannedReplyService.deleteCannedReply(replyId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/canned-replies/{replyId} - Get canned reply by ID
     */
    @GetMapping("/{replyId}")
    public ResponseEntity<CannedReplyResponse> getCannedReplyById(@PathVariable String replyId) {
        CannedReplyResponse response = cannedReplyService.getCannedReplyById(replyId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/canned-replies - List canned replies
     */
    @GetMapping
    public ResponseEntity<CannedReplyListResponse> listCannedReplies(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String locale) {
        CannedReplyListResponse response = cannedReplyService.listByCategory(category, locale);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/canned-replies/shortcuts/{shortcuts} - Find by shortcuts
     */
    @GetMapping("/shortcuts/{shortcuts}")
    public ResponseEntity<CannedReplyResponse> findByShortcuts(@PathVariable String shortcuts) {
        CannedReplyResponse response = cannedReplyService.findByShortcuts(shortcuts);
        return ResponseEntity.ok(response);
    }
}
