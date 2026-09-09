-- ========================================
-- Clean Test Data from CHAT_APP Schema
-- Use this to reset data between test runs
-- Run in CHAT_APP schema
-- ========================================

-- Delete in order (respect foreign keys)
DELETE FROM CHAT_AUDIT_LOG;
DELETE FROM CHAT_ASSIGNMENT_HISTORY;
DELETE FROM CHAT_ATTACHMENT;
DELETE FROM CHAT_MESSAGE;
DELETE FROM CHAT_CONTEXT;
DELETE FROM CHAT_OUTBOX;
DELETE FROM CHAT_CONVERSATION;

COMMIT;

-- Verify - should all return 0
SELECT 
    (SELECT COUNT(*) FROM CHAT_CONVERSATION) AS conversations,
    (SELECT COUNT(*) FROM CHAT_MESSAGE) AS messages,
    (SELECT COUNT(*) FROM CHAT_CONTEXT) AS contexts,
    (SELECT COUNT(*) FROM CHAT_ATTACHMENT) AS attachments,
    (SELECT COUNT(*) FROM CHAT_OUTBOX) AS outbox_events,
    (SELECT COUNT(*) FROM CHAT_ASSIGNMENT_HISTORY) AS assignments,
    (SELECT COUNT(*) FROM CHAT_AUDIT_LOG) AS audit_logs
FROM DUAL;

SELECT 'Test data cleaned successfully' AS status FROM DUAL;
