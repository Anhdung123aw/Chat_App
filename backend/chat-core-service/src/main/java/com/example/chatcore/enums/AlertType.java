package com.example.chatcore.enums;

/**
 * Loại cảnh báo cho hệ thống vận hành
 */
public enum AlertType {
    /**
     * Cảnh báo khi số lượng conversation trong queue vượt ngưỡng
     * Ví dụ: queue > 50 conversations
     */
    QUEUE_DEPTH,

    /**
     * Cảnh báo khi số lượng vi phạm SLA vượt ngưỡng
     * Ví dụ: > 10 vi phạm trong 1 giờ
     */
    SLA_BREACH,

    /**
     * Cảnh báo khi số lượng agent online thấp
     * Ví dụ: < 3 agents online
     */
    LOW_AGENT_CAPACITY,

    /**
     * Cảnh báo khi thời gian chờ trung bình cao
     * Ví dụ: avg wait time > 10 phút
     */
    HIGH_WAIT_TIME
}
