package com.example.chatcore.exception;

/**
 * Exception khi không tìm thấy agent
 */
public class AgentNotFoundException extends RuntimeException {
    public AgentNotFoundException(String message) {
        super(message);
    }

    public AgentNotFoundException(String agentId, String reason) {
        super(String.format("Agent %s không tồn tại: %s", agentId, reason));
    }
}
