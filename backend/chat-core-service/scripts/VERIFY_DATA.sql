-- ========================================
-- VERIFY DATA sau khi test API
-- Chạy trong DBeaver (connection CHAT_APP)
-- ========================================

-- 1. Xem conversation vừa tạo
SELECT * FROM CHAT_CONVERSATION ORDER BY CREATED_AT DESC;

-- 2. Xem outbox event (Kafka event)
SELECT EVENT_ID, AGGREGATE_ID, EVENT_TYPE, STATUS, CREATED_AT, PUBLISHED_AT 
FROM CHAT_OUTBOX 
ORDER BY CREATED_AT DESC;

-- 3. Xem payload của event
SELECT EVENT_TYPE, PAYLOAD 
FROM CHAT_OUTBOX 
ORDER BY CREATED_AT DESC 
FETCH FIRST 1 ROWS ONLY;

-- 4. Count records
SELECT 
    (SELECT COUNT(*) FROM CHAT_CONVERSATION) AS total_conversations,
    (SELECT COUNT(*) FROM CHAT_OUTBOX) AS total_outbox_events
FROM DUAL;
