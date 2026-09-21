-- ============================================================================
-- Phase 2 - Operations Tables
-- Description: Auto assignment, SLA, Canned reply, Rating, Alerting
-- Author: Chat App Team
-- Date: 2026-09-08
-- ============================================================================

-- ============================================================================
-- 1. CHAT_AGENT - Quản lý thông tin Agent
-- ============================================================================
CREATE TABLE CHAT_AGENT (
    AGENT_ID VARCHAR2(50) PRIMARY KEY,
    AGENT_NAME NVARCHAR2(100) NOT NULL,
    EMAIL VARCHAR2(100),
    STATUS VARCHAR2(20) NOT NULL CHECK (STATUS IN ('ONLINE', 'OFFLINE', 'BUSY', 'AWAY')),
    SKILLS VARCHAR2(500), -- JSON array: ["ORDER_SUPPORT", "PAYMENT", "ACCOUNT"]
    MAX_CONCURRENT_CHATS NUMBER(3) DEFAULT 5 NOT NULL,
    CURRENT_CHAT_COUNT NUMBER(3) DEFAULT 0 NOT NULL,
    LAST_ACTIVE_AT TIMESTAMP,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Index cho query agent theo status
CREATE INDEX IDX_AGENT_STATUS ON CHAT_AGENT(STATUS);
CREATE INDEX IDX_AGENT_LAST_ACTIVE ON CHAT_AGENT(LAST_ACTIVE_AT);

COMMENT ON TABLE CHAT_AGENT IS 'Bảng quản lý thông tin CS Agent';
COMMENT ON COLUMN CHAT_AGENT.AGENT_ID IS 'ID duy nhất của agent';
COMMENT ON COLUMN CHAT_AGENT.STATUS IS 'Trạng thái agent: ONLINE, OFFLINE, BUSY, AWAY';
COMMENT ON COLUMN CHAT_AGENT.SKILLS IS 'Danh sách kỹ năng/topic agent có thể xử lý (JSON)';
COMMENT ON COLUMN CHAT_AGENT.MAX_CONCURRENT_CHATS IS 'Số lượng chat tối đa agent có thể xử lý cùng lúc';
COMMENT ON COLUMN CHAT_AGENT.CURRENT_CHAT_COUNT IS 'Số lượng chat hiện tại agent đang xử lý';

-- ============================================================================
-- 2. CHAT_SLA_CONFIG - Cấu hình SLA theo topic
-- ============================================================================
CREATE TABLE CHAT_SLA_CONFIG (
    CONFIG_ID VARCHAR2(50) PRIMARY KEY,
    TOPIC_CODE VARCHAR2(50) NOT NULL UNIQUE,
    FIRST_RESPONSE_TIME_SECONDS NUMBER(10) NOT NULL, -- Thời gian agent phải phản hồi lần đầu
    RESOLUTION_TIME_SECONDS NUMBER(10), -- Thời gian giải quyết hoàn toàn (optional)
    ENABLED NUMBER(1) DEFAULT 1 NOT NULL CHECK (ENABLED IN (0, 1)),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IDX_SLA_TOPIC ON CHAT_SLA_CONFIG(TOPIC_CODE);

COMMENT ON TABLE CHAT_SLA_CONFIG IS 'Cấu hình SLA (Service Level Agreement) theo chủ đề';
COMMENT ON COLUMN CHAT_SLA_CONFIG.TOPIC_CODE IS 'Mã chủ đề: ORDER_SUPPORT, PAYMENT, ACCOUNT...';
COMMENT ON COLUMN CHAT_SLA_CONFIG.FIRST_RESPONSE_TIME_SECONDS IS 'SLA cho phản hồi đầu tiên (giây)';
COMMENT ON COLUMN CHAT_SLA_CONFIG.RESOLUTION_TIME_SECONDS IS 'SLA cho giải quyết hoàn toàn (giây)';

-- Insert default SLA configs
INSERT INTO CHAT_SLA_CONFIG (CONFIG_ID, TOPIC_CODE, FIRST_RESPONSE_TIME_SECONDS, RESOLUTION_TIME_SECONDS, ENABLED)
VALUES ('SLA001', 'ORDER_SUPPORT', 120, 3600, 1); -- 2 phút phản hồi, 1 giờ giải quyết

INSERT INTO CHAT_SLA_CONFIG (CONFIG_ID, TOPIC_CODE, FIRST_RESPONSE_TIME_SECONDS, RESOLUTION_TIME_SECONDS, ENABLED)
VALUES ('SLA002', 'PAYMENT', 180, 7200, 1); -- 3 phút phản hồi, 2 giờ giải quyết

INSERT INTO CHAT_SLA_CONFIG (CONFIG_ID, TOPIC_CODE, FIRST_RESPONSE_TIME_SECONDS, RESOLUTION_TIME_SECONDS, ENABLED)
VALUES ('SLA003', 'ACCOUNT', 300, 86400, 1); -- 5 phút phản hồi, 24 giờ giải quyết

INSERT INTO CHAT_SLA_CONFIG (CONFIG_ID, TOPIC_CODE, FIRST_RESPONSE_TIME_SECONDS, RESOLUTION_TIME_SECONDS, ENABLED)
VALUES ('SLA999', 'DEFAULT', 240, 14400, 1); -- 4 phút phản hồi, 4 giờ giải quyết (fallback)

COMMIT;

-- ============================================================================
-- 3. CHAT_CANNED_REPLY - Template trả lời nhanh cho Agent
-- ============================================================================
CREATE TABLE CHAT_CANNED_REPLY (
    REPLY_ID VARCHAR2(50) PRIMARY KEY,
    CATEGORY VARCHAR2(50) NOT NULL, -- GREETING, CLOSING, FAQ, TROUBLESHOOTING
    TITLE NVARCHAR2(200) NOT NULL,
    CONTENT NCLOB NOT NULL,
    SHORTCUTS VARCHAR2(50), -- /hello, /thanks, /bye
    LOCALE VARCHAR2(10) DEFAULT 'vi' NOT NULL,
    CREATED_BY VARCHAR2(50),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IDX_CANNED_CATEGORY ON CHAT_CANNED_REPLY(CATEGORY);
CREATE INDEX IDX_CANNED_SHORTCUTS ON CHAT_CANNED_REPLY(SHORTCUTS);

COMMENT ON TABLE CHAT_CANNED_REPLY IS 'Template trả lời nhanh cho agent';
COMMENT ON COLUMN CHAT_CANNED_REPLY.CATEGORY IS 'Danh mục: GREETING, CLOSING, FAQ, TROUBLESHOOTING';
COMMENT ON COLUMN CHAT_CANNED_REPLY.SHORTCUTS IS 'Phím tắt để agent gọi nhanh template';

-- Insert sample canned replies
INSERT INTO CHAT_CANNED_REPLY (REPLY_ID, CATEGORY, TITLE, CONTENT, SHORTCUTS, LOCALE, CREATED_BY)
VALUES ('CR001', 'GREETING', 'Chào mừng khách hàng', 'Xin chào! Tôi là {agentName}. Tôi có thể giúp gì cho bạn hôm nay?', '/hello', 'vi', 'system');

INSERT INTO CHAT_CANNED_REPLY (REPLY_ID, CATEGORY, TITLE, CONTENT, SHORTCUTS, LOCALE, CREATED_BY)
VALUES ('CR002', 'CLOSING', 'Cảm ơn và kết thúc', 'Cảm ơn bạn đã liên hệ. Nếu cần hỗ trợ thêm, hãy chat lại nhé!', '/thanks', 'vi', 'system');

INSERT INTO CHAT_CANNED_REPLY (REPLY_ID, CATEGORY, TITLE, CONTENT, SHORTCUTS, LOCALE, CREATED_BY)
VALUES ('CR003', 'FAQ', 'Hướng dẫn kiểm tra đơn hàng', 'Bạn có thể kiểm tra đơn hàng bằng cách: 1. Vào menu "Đơn hàng của tôi" 2. Chọn đơn hàng cần xem 3. Xem chi tiết trạng thái', '/order', 'vi', 'system');

INSERT INTO CHAT_CANNED_REPLY (REPLY_ID, CATEGORY, TITLE, CONTENT, SHORTCUTS, LOCALE, CREATED_BY)
VALUES ('CR004', 'TROUBLESHOOTING', 'Yêu cầu thông tin thêm', 'Để tôi có thể hỗ trợ tốt hơn, bạn vui lòng cung cấp: 1. Mã đơn hàng 2. Thời gian gặp sự cố 3. Screenshot nếu có', '/info', 'vi', 'system');

COMMIT;

-- ============================================================================
-- 4. CHAT_RATING - Đánh giá CSAT từ khách hàng
-- ============================================================================
CREATE TABLE CHAT_RATING (
    RATING_ID VARCHAR2(50) PRIMARY KEY,
    CONVERSATION_ID VARCHAR2(50) NOT NULL,
    RATING NUMBER(1) NOT NULL CHECK (RATING BETWEEN 1 AND 5),
    CUSTOMER_COMMENT NCLOB,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT FK_RATING_CONVERSATION FOREIGN KEY (CONVERSATION_ID) 
        REFERENCES CHAT_CONVERSATION(CONVERSATION_ID)
);

-- Unique constraint: 1 conversation chỉ được rate 1 lần
CREATE UNIQUE INDEX UNQ_RATING_CONVERSATION ON CHAT_RATING(CONVERSATION_ID);
CREATE INDEX IDX_RATING_CREATED ON CHAT_RATING(CREATED_AT);

COMMENT ON TABLE CHAT_RATING IS 'Đánh giá CSAT (Customer Satisfaction) từ khách hàng';
COMMENT ON COLUMN CHAT_RATING.RATING IS 'Điểm đánh giá từ 1-5 (1: rất không hài lòng, 5: rất hài lòng)';

-- ============================================================================
-- 5. CHAT_ALERT_CONFIG - Cấu hình cảnh báo vận hành
-- ============================================================================
CREATE TABLE CHAT_ALERT_CONFIG (
    ALERT_ID VARCHAR2(50) PRIMARY KEY,
    ALERT_TYPE VARCHAR2(50) NOT NULL, -- QUEUE_DEPTH, SLA_BREACH, LOW_AGENT_CAPACITY, HIGH_WAIT_TIME
    THRESHOLD NUMBER(10) NOT NULL, -- Ngưỡng kích hoạt cảnh báo
    NOTIFICATION_CHANNELS VARCHAR2(200), -- JSON: ["slack", "email", "webhook"]
    ENABLED NUMBER(1) DEFAULT 1 NOT NULL CHECK (ENABLED IN (0, 1)),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IDX_ALERT_TYPE ON CHAT_ALERT_CONFIG(ALERT_TYPE);

COMMENT ON TABLE CHAT_ALERT_CONFIG IS 'Cấu hình cảnh báo cho hệ thống vận hành';
COMMENT ON COLUMN CHAT_ALERT_CONFIG.ALERT_TYPE IS 'Loại cảnh báo: QUEUE_DEPTH, SLA_BREACH, LOW_AGENT_CAPACITY';
COMMENT ON COLUMN CHAT_ALERT_CONFIG.THRESHOLD IS 'Ngưỡng kích hoạt cảnh báo';

-- Insert default alert configs
INSERT INTO CHAT_ALERT_CONFIG (ALERT_ID, ALERT_TYPE, THRESHOLD, NOTIFICATION_CHANNELS, ENABLED)
VALUES ('ALT001', 'QUEUE_DEPTH', 50, '["slack"]', 1); -- Cảnh báo khi queue > 50

INSERT INTO CHAT_ALERT_CONFIG (ALERT_ID, ALERT_TYPE, THRESHOLD, NOTIFICATION_CHANNELS, ENABLED)
VALUES ('ALT002', 'SLA_BREACH', 10, '["slack", "email"]', 1); -- Cảnh báo khi vi phạm SLA > 10 trong 1 giờ

INSERT INTO CHAT_ALERT_CONFIG (ALERT_ID, ALERT_TYPE, THRESHOLD, NOTIFICATION_CHANNELS, ENABLED)
VALUES ('ALT003', 'LOW_AGENT_CAPACITY', 3, '["slack"]', 1); -- Cảnh báo khi agent online < 3

COMMIT;

-- ============================================================================
-- 6. Update existing table CHAT_CONVERSATION - Thêm SLA_DEADLINE
-- ============================================================================
ALTER TABLE CHAT_CONVERSATION ADD SLA_DEADLINE TIMESTAMP;

COMMENT ON COLUMN CHAT_CONVERSATION.SLA_DEADLINE IS 'Thời hạn SLA (CREATED_AT + SLA_CONFIG.firstResponseTimeSeconds)';

CREATE INDEX IDX_CONVERSATION_SLA ON CHAT_CONVERSATION(SLA_DEADLINE);

-- ============================================================================
-- END OF MIGRATION V3
-- ============================================================================
