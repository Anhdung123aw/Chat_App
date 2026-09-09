# Chat Core Service

REST API service for managing chat conversations, messages, and agent assignments.

## 🚀 Features

- **Conversation Management**: Create, retrieve, assign, transfer, and close conversations
- **Message Handling**: Send/receive messages with read tracking
- **Agent Assignment**: Auto/manual assignment with transfer capability
- **Context Capture**: Store user context (screen, device, app version)
- **Audit Logging**: Track all conversation state changes
- **Outbox Pattern**: Reliable event publishing to Kafka
- **Attachment Support**: Metadata storage for MinIO integration

## 🏗️ Tech Stack

- **Java 21** + Spring Boot 4.1.1
- **Spring Data JPA** + Hibernate 7.4.5
- **Oracle Database 21c** (XE)
- **Kafka** (event streaming - via Outbox)
- **MinIO** (object storage)
- **Maven** (build tool)

## 📋 Prerequisites

- JDK 21+
- Maven 3.9+
- Oracle Database 21c (running on localhost:1521)
- Schema: `CHAT_APP` / Password: `123`

## 🔧 Setup

### 1. Database Setup
```sql
-- Run in DBeaver with SYSTEM user
@scripts/setup-oracle-db.sql
```

### 2. Tables Creation
Tables will be created automatically by Flyway on first startup.
Migration file: `src/main/resources/db/migration/V1__init_chat_core.sql`

### 3. Build
```bash
mvn clean install
```

### 4. Run
```bash
mvn spring-boot:run
```

App will start on `http://localhost:8080`

## 📚 API Documentation

### Health Check
```
GET /actuator/health
```

### Conversations

**Create Conversation**
```http
POST /api/v1/conversations
Content-Type: application/json

{
  "userId": "user123",
  "merchantId": "merchant001",
  "topicCode": "ACCOUNT",
  "context": {
    "screenName": "ACCOUNT_MANAGEMENT",
    "feature": "VIEW_BALANCE",
    "lastAction": "TAP_SUPPORT"
  }
}
```

**Get Conversation**
```http
GET /api/v1/conversations/{id}
```

**Get Queue (NEW conversations)**
```http
GET /api/v1/conversations/queue
```

**Assign Agent**
```http
POST /api/v1/conversations/{id}/assign
{
  "agentId": "agent001"
}
```

**Transfer Conversation**
```http
POST /api/v1/conversations/{id}/transfer
{
  "toAgentId": "agent002",
  "transferredBy": "agent001",
  "reason": "Specialist required"
}
```

**Close Conversation**
```http
POST /api/v1/conversations/{id}/close
```

### Messages

**Send Message**
```http
POST /api/v1/conversations/{id}/messages
{
  "senderId": "user123",
  "senderType": "USER",
  "messageType": "TEXT",
  "content": "Hello, I need help",
  "clientMessageId": "msg-001"
}
```

**Get Message History**
```http
GET /api/v1/conversations/{id}/messages
```

**Mark Messages as Read**
```http
POST /api/v1/conversations/{id}/read?upToSequence=5
```

### Attachments

**Request Upload URL**
```http
POST /api/v1/attachments/upload-request
{
  "conversationId": "{id}",
  "fileName": "screenshot.png",
  "contentType": "image/png",
  "fileSize": 524288
}
```

**Get Attachment**
```http
GET /api/v1/attachments/{attachmentId}
```

## 🧪 Testing

### Automated API Tests
```powershell
cd scripts
.\test-all-apis.ps1
```

### Postman Collection
Import `../../postman/Chat-App-API.postman_collection.json`

See `../../postman/TEST_GUIDE.md` for detailed test scenarios.

### Verify Database
```sql
-- Run in DBeaver (CHAT_APP schema)
@scripts/VERIFY_DATA.sql
```

### Clean Test Data
```sql
@scripts/clean-test-data.sql
```

## 📁 Project Structure

```
src/main/java/com/example/chatcore/
├── assignment/          # Agent assignment history
├── attachment/          # File attachment metadata
├── audit/              # Audit logging with AOP
├── common/             # Outbox pattern, exception handling
├── config/             # Spring configuration
├── context/            # User context capture
├── conversation/       # Conversation management
└── message/            # Message handling
```

## 🗄️ Database Schema

8 tables in `CHAT_APP` schema:
- `CHAT_CONVERSATION` - Main conversation records
- `CHAT_MESSAGE` - Messages with sequence numbers
- `CHAT_CONTEXT` - User context snapshot
- `CHAT_ATTACHMENT` - File metadata
- `CHAT_OUTBOX` - Event outbox for Kafka
- `CHAT_ASSIGNMENT_HISTORY` - Agent assignment log
- `CHAT_AUDIT_LOG` - Audit trail
- `FLYWAY_SCHEMA_HISTORY` - Migration tracking

## ⚙️ Configuration

**application.yml**
```yaml
app:
  security:
    enabled: false  # Set true for OAuth2 JWT

spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/XEPDB1
    username: CHAT_APP
    password: 123

  kafka:
    bootstrap-servers: localhost:9092

minio:
  endpoint: http://localhost:9000
```

## 🔐 Security

**Development Mode** (current):
- Security disabled (`app.security.enabled=false`)
- All endpoints accessible without authentication

**Production Mode**:
- Enable OAuth2 JWT authentication
- Role-based access control (agent, supervisor, admin)
- Method-level security with `@PreAuthorize`

## 📊 Monitoring

**Actuator Endpoints**:
- `/actuator/health` - Health check
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus format metrics

## 🐛 Troubleshooting

**Tables not created**:
- Check Flyway logs in startup
- Manually run `V1__init_chat_core.sql` in CHAT_APP schema

**403 Forbidden on APIs**:
- Ensure `app.security.enabled=false` in application.yml
- `@EnableMethodSecurity` is disabled in SecurityConfig

**Connection refused**:
- Verify Oracle DB is running on port 1521
- Check CHAT_APP user has correct permissions

## 📝 License

Internal project - not for public distribution

## 👥 Team

Backend Development Team - Phase 1 MVP

---

**Version**: 0.0.1-SNAPSHOT  
**Status**: ✅ Phase 1 Complete
