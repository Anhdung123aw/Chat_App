package com.example.chatcore.controller;

import com.example.chatcore.dto.MessageDTO.*;
import com.example.chatcore.entity.ChatMessageEntity;
import com.example.chatcore.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@PathVariable String conversationId,
                                @Valid @RequestBody SendMessageRequest request) {
        ChatMessageEntity message = messageService.sendMessage(
                conversationId, request.senderType(), request.senderId(),
                request.messageType(), request.content(), request.clientMessageId());
        return MessageResponse.from(message);
    }

    @GetMapping
    public List<MessageResponse> getMessages(@PathVariable String conversationId,
                                             @RequestParam(required = false) Long afterSequence) {
        List<ChatMessageEntity> messages = afterSequence != null
                ? messageService.syncAfter(conversationId, afterSequence)
                : messageService.getHistory(conversationId);
        return messages.stream().map(MessageResponse::from).toList();
    }

    @PostMapping("/{messageId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable String conversationId, @PathVariable String messageId) {
        messageService.markAsRead(conversationId, messageId);
    }
}