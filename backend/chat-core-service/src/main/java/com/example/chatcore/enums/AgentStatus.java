package com.example.chatcore.enums;

/**
 * Trạng thái của Agent
 */
public enum AgentStatus {
    /**
     * Agent đang online và sẵn sàng nhận chat
     */
    ONLINE,

    /**
     * Agent đã offline, không nhận chat mới
     */
    OFFLINE,

    /**
     * Agent đang bận, không nhận chat mới
     */
    BUSY,

    /**
     * Agent tạm vắng (đi ăn, nghỉ giải lao)
     */
    AWAY
}
