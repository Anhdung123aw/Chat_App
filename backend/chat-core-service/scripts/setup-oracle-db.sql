-- ========================================
-- Setup Oracle Database for Chat Core Service
-- Run this script with DBA privileges (SYSTEM user)
-- ========================================

-- Step 1: Create user/schema CHAT_APP (if not exists)
BEGIN
    EXECUTE IMMEDIATE 'CREATE USER CHAT_APP IDENTIFIED BY 123';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1920 THEN
            DBMS_OUTPUT.PUT_LINE('User CHAT_APP already exists');
        ELSE
            RAISE;
        END IF;
END;
/

-- Step 2: Grant permissions
GRANT CONNECT, RESOURCE, CREATE SESSION TO CHAT_APP;
GRANT UNLIMITED TABLESPACE TO CHAT_APP;

-- Step 3: Verify
SELECT 'User CHAT_APP created successfully' AS status FROM DUAL;

-- ========================================
-- IMPORTANT: After running this script
-- ========================================
-- 1. Tables will be created automatically by Flyway migration
--    (src/main/resources/db/migration/V1__init_chat_core.sql)
-- 2. Or manually run the V1 migration script in CHAT_APP schema
-- ========================================
