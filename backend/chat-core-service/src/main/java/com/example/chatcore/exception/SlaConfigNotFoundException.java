package com.example.chatcore.exception;

/**
 * Exception khi không tìm thấy SLA config
 */
public class SlaConfigNotFoundException extends RuntimeException {
    public SlaConfigNotFoundException(String message) {
        super(message);
    }

    public static SlaConfigNotFoundException forTopic(String topicCode) {
        return new SlaConfigNotFoundException(
                String.format("Không tìm thấy SLA config cho topic: %s", topicCode)
        );
    }
}
