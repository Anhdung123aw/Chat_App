# Postman Collection - Chat App API

API testing collection for Chat Core Service Phase 1 MVP.

## 📦 Files

- **Chat-App-API.postman_collection.json** - Full API collection with auto variable management
- **TEST_GUIDE.md** - Detailed test guide with expected responses

## 🚀 Quick Start

### 1. Import Collection
1. Open Postman
2. Click **Import**
3. Select `Chat-App-API.postman_collection.json`

### 2. Configure Variables
Collection variables (auto-managed):
- `baseUrl` = `http://localhost:8080`
- `conversationId` = auto-saved after creating conversation
- `messageId` = auto-saved after sending message

### 3. Run Tests
Execute requests in order:
1. **Health Check** - Verify service is running
2. **Create Conversation** - Creates new conversation (saves ID)
3. **Send Message** - Send user message
4. **Get Queue** - View pending conversations
5. **Assign Agent** - Assign conversation to agent
6. **Transfer** - Transfer to another agent
7. **Close** - Close conversation

## 📋 Test Flow

Follow the complete test flow in **TEST_GUIDE.md**:
- Step-by-step instructions
- Expected responses
- Database verification queries
- Troubleshooting tips

## ⚙️ Prerequisites

- Spring Boot app running on `localhost:8080`
- Oracle DB with CHAT_APP schema
- Postman installed

## 🔍 Features

- ✅ Auto variable management (conversationId, messageId)
- ✅ Pre-request scripts
- ✅ Response assertions
- ✅ Collection-level configuration

## 📝 Notes

- **No authentication required** (dev mode with `app.security.enabled=false`)
- Vietnamese characters supported in message content
- All endpoints return JSON responses
