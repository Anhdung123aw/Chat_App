package com.example.chatcore.exception;

public class ConversationConflictException extends RuntimeException {
    public ConversationConflictException(String message) {
        super(message);
    }
}