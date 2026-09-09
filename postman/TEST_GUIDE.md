# 🧪 Chat App API - Test Guide (Phase 1 MVP)

## ✅ Prerequisites
- ✅ Spring Boot app running on `http://localhost:8080`
- ✅ Oracle DB schema `CHAT_APP` với 8 bảng
- ✅ Postman collection imported: `Chat-App-API.postman_collection.json`

---

## 📋 Test Flow - Complete Conversation Lifecycle

### **STEP 1: Health Check** ✅
**Purpose:** Verify app is running

**API:** `GET /actuator/health`

**Expected Response:**
```json
{
  "status": "UP"
}
```

---

### **STEP 2: Create Conversation** 🆕
**Purpose:** User khởi tạo conversation mới

**API:** `POST /api/v1/conversations`

**Request Body:**
```json
{
  "userId": "user123",
  "merchantId": "merchant001",
  "topicCode": "ACCOUNT",
  "context": {
    "screenName": "ACCOUNT_MANAGEMENT",
    "feature": "VIEW_BALANCE",
    "lastAction": "TAP_SUPPORT",
    "appVersion": "1.2.5",
    "os": "Android",
    "device": "Samsung Galaxy S23"
  }
}
```

**Expected Response (201 Created):**
```json
{
  "conversationId": "uuid-here",
  "userId": "user123",
  "merchantId": "merchant001",
  "topicCode": "ACCOUNT",
  "status": "NEW",
  "assignedAgent": null,
  "createdAt": "2026-09-07T06:52:25.400Z",
  "updatedAt": "2026-09-07T06:52:25.400Z"
}
```

**✅ Verify in DB:**
```sql
SELECT * FROM CHAT_CONVERSATION WHERE USER_ID = 'user123';
SELECT * FROM CHAT_CONTEXT WHERE CONVERSATION_ID = '<conversationId>';
SELECT * FROM CHAT_OUTBOX WHERE EVENT_TYPE = 'chat.conversation.created';
```

**📝 Note:** Postman sẽ tự động lưu `conversationId` vào collection variable!

---

### **STEP 3: Send User Message** 💬
**Purpose:** User gửi tin nhắn đầu tiên

**API:** `POST /api/v1/conversations/{{conversationId}}/messages`

**Request Body:**
```json
{
  "senderId": "user123",
  "senderType": "USER",
  "messageType": "TEXT",
  "content": "Xin chào, tôi cần hỗ trợ về tài khoản",
  "clientMessageId": "client-msg-001"
}
```

**Expected Response (201 Created):**
```json
{
  "messageId": "uuid-here",
  "conversationId": "<conversationId>",
  "senderType": "USER",
  "senderId": "user123",
  "messageType": "TEXT",
  "content": "Xin chào, tôi cần hỗ trợ về tài khoản",
  "seqNo": 1,
  "createdAt": "2026-09-07T06:53:00.000Z"
}
```

**✅ Verify in DB:**
```sql
SELECT * FROM CHAT_MESSAGE WHERE CONVERSATION_ID = '<conversationId>' ORDER BY SEQ_NO;
SELECT * FROM CHAT_OUTBOX WHERE EVENT_TYPE = 'chat.message.sent';
```

---

### **STEP 4: Get Queue (Agent View)** 📥
**Purpose:** Agent xem danh sách conversation chưa assign

**API:** `GET /api/v1/conversations/queue`

**Expected Response (200 OK):**
```json
[
  {
    "conversationId": "<conversationId>",
    "userId": "user123",
    "topicCode": "ACCOUNT",
    "status": "NEW",
    "createdAt": "2026-09-07T06:52:25.400Z"
  }
]
```

---

### **STEP 5: Assign Agent** 👤
**Purpose:** Conversation được gán cho agent

**API:** `POST /api/v1/conversations/{{conversationId}}/assign`

**Request Body:**
```json
{
  "agentId": "agent001"
}
```

**Expected Response (200 OK):**
```json
{
  "conversationId": "<conversationId>",
  "userId": "user123",
  "status": "ASSIGNED",
  "assignedAgent": "agent001",
  "updatedAt": "2026-09-07T06:54:00.000Z"
}
```

**✅ Verify in DB:**
```sql
SELECT * FROM CHAT_CONVERSATION WHERE CONVERSATION_ID = '<conversationId>';
SELECT * FROM CHAT_ASSIGNMENT_HISTORY WHERE CONVERSATION_ID = '<conversationId>';
SELECT * FROM CHAT_AUDIT_LOG WHERE CONVERSATION_ID = '<conversationId>' AND EVENT_TYPE = 'ASSIGN';
```

---

### **STEP 6: Send Agent Reply** 💬
**Purpose:** Agent trả lời user

**API:** `POST /api/v1/conversations/{{conversationId}}/messages`

**Request Body:**
```json
{
  "senderId": "agent001",
  "senderType": "AGENT",
  "messageType": "TEXT",
  "content": "Xin chào! Tôi có thể giúp gì cho bạn?",
  "clientMessageId": "agent-msg-001"
}
```

**Expected Response (201 Created):**
```json
{
  "messageId": "uuid-here",
  "seqNo": 2,
  "senderType": "AGENT",
  "senderId": "agent001",
  "content": "Xin chào! Tôi có thể giúp gì cho bạn?"
}
```

---

### **STEP 7: Get Message History** 📜
**Purpose:** Xem toàn bộ lịch sử tin nhắn

**API:** `GET /api/v1/conversations/{{conversationId}}/messages`

**Expected Response (200 OK):**
```json
[
  {
    "messageId": "uuid-1",
    "seqNo": 1,
    "senderType": "USER",
    "senderId": "user123",
    "content": "Xin chào, tôi cần hỗ trợ về tài khoản",
    "createdAt": "2026-09-07T06:53:00.000Z"
  },
  {
    "messageId": "uuid-2",
    "seqNo": 2,
    "senderType": "AGENT",
    "senderId": "agent001",
    "content": "Xin chào! Tôi có thể giúp gì cho bạn?",
    "createdAt": "2026-09-07T06:54:30.000Z"
  }
]
```

---

### **STEP 8: Mark Messages as Read** ✅
**Purpose:** Đánh dấu tin nhắn đã đọc

**API:** `POST /api/v1/conversations/{{conversationId}}/read?upToSequence=2`

**Expected Response (200 OK):**
```json
{
  "markedCount": 2
}
```

**✅ Verify in DB:**
```sql
SELECT MESSAGE_ID, SEQ_NO, READ_AT FROM CHAT_MESSAGE 
WHERE CONVERSATION_ID = '<conversationId>' 
ORDER BY SEQ_NO;
```

---

### **STEP 9: Transfer to Another Agent** 🔄
**Purpose:** Chuyển conversation sang agent khác

**API:** `POST /api/v1/conversations/{{conversationId}}/transfer`

**Request Body:**
```json
{
  "toAgentId": "agent002",
  "transferredBy": "agent001",
  "reason": "Specialist required"
}
```

**Expected Response (200 OK):**
```json
{
  "conversationId": "<conversationId>",
  "status": "ASSIGNED",
  "assignedAgent": "agent002",
  "updatedAt": "2026-09-07T06:55:00.000Z"
}
```

**✅ Verify in DB:**
```sql
SELECT * FROM CHAT_ASSIGNMENT_HISTORY WHERE CONVERSATION_ID = '<conversationId>' ORDER BY CREATED_AT;
SELECT * FROM CHAT_AUDIT_LOG WHERE CONVERSATION_ID = '<conversationId>' AND EVENT_TYPE = 'TRANSFER';
SELECT * FROM CHAT_OUTBOX WHERE EVENT_TYPE = 'chat.conversation.transferred';
```

---

### **STEP 10: Close Conversation** 🔒
**Purpose:** Kết thúc conversation

**API:** `POST /api/v1/conversations/{{conversationId}}/close`

**Expected Response (200 OK):**
```json
{
  "conversationId": "<conversationId>",
  "status": "CLOSED",
  "closedAt": "2026-09-07T06:56:00.000Z"
}
```

**✅ Verify in DB:**
```sql
SELECT CONVERSATION_ID, STATUS, CLOSED_AT FROM CHAT_CONVERSATION WHERE CONVERSATION_ID = '<conversationId>';
SELECT * FROM CHAT_AUDIT_LOG WHERE CONVERSATION_ID = '<conversationId>' AND EVENT_TYPE = 'CLOSE';
SELECT * FROM CHAT_OUTBOX WHERE EVENT_TYPE = 'chat.conversation.closed';
```

---

## 🔍 Additional Test Cases

### **Test Case: Get Conversation by ID**
**API:** `GET /api/v1/conversations/{{conversationId}}`

### **Test Case: List User Conversations**
**API:** `GET /api/v1/conversations?userId=user123`

### **Test Case: Sync New Messages**
**API:** `GET /api/v1/conversations/{{conversationId}}/messages?afterSequence=2`

### **Test Case: Request Upload URL (Attachment)**
**API:** `POST /api/v1/attachments/upload-request`
```json
{
  "conversationId": "{{conversationId}}",
  "fileName": "screenshot.png",
  "contentType": "image/png",
  "fileSize": 524288
}
```

---

## 📊 Final Verification Queries

Chạy trong **DBeaver** sau khi test xong:

```sql
-- Tổng quan toàn bộ data
SELECT 
    'CHAT_CONVERSATION' AS table_name, COUNT(*) AS total_records FROM CHAT_CONVERSATION
UNION ALL
SELECT 'CHAT_MESSAGE', COUNT(*) FROM CHAT_MESSAGE
UNION ALL
SELECT 'CHAT_CONTEXT', COUNT(*) FROM CHAT_CONTEXT
UNION ALL
SELECT 'CHAT_OUTBOX', COUNT(*) FROM CHAT_OUTBOX
UNION ALL
SELECT 'CHAT_ASSIGNMENT_HISTORY', COUNT(*) FROM CHAT_ASSIGNMENT_HISTORY
UNION ALL
SELECT 'CHAT_AUDIT_LOG', COUNT(*) FROM CHAT_AUDIT_LOG;

-- Xem toàn bộ flow của 1 conversation
SELECT 
    c.CONVERSATION_ID,
    c.STATUS,
    c.ASSIGNED_AGENT,
    c.CREATED_AT,
    c.CLOSED_AT,
    (SELECT COUNT(*) FROM CHAT_MESSAGE WHERE CONVERSATION_ID = c.CONVERSATION_ID) AS message_count,
    (SELECT COUNT(*) FROM CHAT_ASSIGNMENT_HISTORY WHERE CONVERSATION_ID = c.CONVERSATION_ID) AS assignment_count,
    (SELECT COUNT(*) FROM CHAT_AUDIT_LOG WHERE CONVERSATION_ID = c.CONVERSATION_ID) AS audit_count
FROM CHAT_CONVERSATION c
WHERE c.USER_ID = 'user123';

-- Xem timeline events
SELECT EVENT_TYPE, AGGREGATE_ID, STATUS, CREATED_AT, PUBLISHED_AT
FROM CHAT_OUTBOX
ORDER BY CREATED_AT;
```

---

## ✅ Success Criteria

Sau khi test xong toàn bộ flow, bạn phải có:

| Table | Expected Records |
|-------|------------------|
| **CHAT_CONVERSATION** | 1 conversation (status=CLOSED) |
| **CHAT_MESSAGE** | 2+ messages (user + agent) |
| **CHAT_CONTEXT** | 1 context record |
| **CHAT_ASSIGNMENT_HISTORY** | 2 records (AUTO assign + HANDOVER) |
| **CHAT_AUDIT_LOG** | 3+ records (ASSIGN, TRANSFER, CLOSE) |
| **CHAT_OUTBOX** | 5+ events (created, messages, assigned, transferred, closed) |

---

## 🐛 Common Errors

### Error: 500 Internal Server Error
**Cause:** Table không tồn tại hoặc constraint violation  
**Fix:** Verify tables exist và data format đúng

### Error: 404 Not Found
**Cause:** `conversationId` không tồn tại  
**Fix:** Copy đúng `conversationId` từ response Step 2

### Error: 409 Conflict - "Conversation already closed"
**Cause:** Đang thao tác trên conversation đã CLOSED  
**Fix:** Tạo conversation mới (chạy lại Step 2)

---

## 📌 Notes

- ⚠️ **Postman tự động lưu `conversationId` và `messageId`** vào collection variables
- 💡 Nếu test lại từ đầu, chạy lại **Step 2** để tạo conversation mới
- 🔍 Luôn verify trong Oracle DB sau mỗi API call quan trọng
- 🚀 Collection này cover **100% Phase 1 MVP APIs**

---

## 🎯 Next Steps After Testing

1. ✅ Verify tất cả APIs work
2. 📝 Document bugs/issues (nếu có)
3. 🐳 Setup Docker Compose (Kafka + MinIO)
4. 🔄 Test Outbox Publisher (publish events to Kafka)
5. 📊 Setup Grafana monitoring

Good luck! 🚀
