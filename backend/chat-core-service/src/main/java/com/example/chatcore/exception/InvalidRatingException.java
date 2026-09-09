package com.example.chatcore.exception;

/**
 * Exception khi rating không hợp lệ
 */
public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(String message) {
        super(message);
    }

    public static InvalidRatingException alreadyRated(String conversationId) {
        return new InvalidRatingException(
                String.format("Conversation %s đã được đánh giá rồi", conversationId)
        );
    }

    public static InvalidRatingException conversationNotClosed(String conversationId) {
        return new InvalidRatingException(
                String.format("Conversation %s chưa đóng, không thể đánh giá", conversationId)
        );
    }
}
