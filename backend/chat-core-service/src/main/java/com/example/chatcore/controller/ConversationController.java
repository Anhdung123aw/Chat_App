package com.example.chatcore.controller;

import com.example.chatcore.dto.ConversationDTO.*;
import com.example.chatcore.entity.ChatConversationEntity;
import com.example.chatcore.enums.ConversationStatus;
import com.example.chatcore.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse create(@Valid @RequestBody CreateConversationRequest request) {
        ChatConversationEntity conversation = conversationService.createConversation(
                request.userId(), request.merchantId(), request.topicCode(), request.context());
        return ConversationResponse.from(conversation);
    }

    @GetMapping("/{id}")
    public ConversationResponse getOne(@PathVariable("id") String conversationId) {
        return ConversationResponse.from(conversationService.getById(conversationId));
    }

    @GetMapping(params = "userId")
    public List<ConversationResponse> listByUser(@RequestParam("userId") String userId) {
        return conversationService.listByUser(userId).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @GetMapping(params = "agentId")
    public List<ConversationResponse> listByAgent(@RequestParam("agentId") String agentId) {
        return conversationService.listByAgent(agentId).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @GetMapping("/queue")
    public List<ConversationResponse> queue() {
        return conversationService.listQueueByStatus(ConversationStatus.NEW).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @PostMapping("/{id}/assign")
    public ConversationResponse assign(@PathVariable("id") String conversationId,
                                       @Valid @RequestBody AssignRequest request) {
        return ConversationResponse.from(conversationService.assign(conversationId, request.agentId()));
    }

    @PostMapping("/{id}/close")
    public ConversationResponse close(@PathVariable("id") String conversationId) {
        return ConversationResponse.from(conversationService.close(conversationId));
    }

    @PostMapping("/{id}/read")
    public ConversationResponse markConversationAsRead(@PathVariable("id") String conversationId,
                                                       @RequestParam Long upToSequence) {
        conversationService.markMessagesAsRead(conversationId, upToSequence);
        return ConversationResponse.from(conversationService.getById(conversationId));
    }

    @PostMapping("/{id}/transfer")
    public ConversationResponse transfer(@PathVariable("id") String conversationId,
                                         @Valid @RequestBody TransferRequest request) {
        return ConversationResponse.from(
                conversationService.transferConversation(conversationId, request.toAgentId(), request.transferredBy()));
    }
}