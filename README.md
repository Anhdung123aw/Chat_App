# Chat Application - Microservices Architecture

Enterprise chat platform with customer support features, built using microservices architecture.

## 🏗️ Architecture Overview

```
┌─────────────────┐
│   Frontend      │
└────────┬────────┘
         │
    ┌────▼──────────────────────────────┐
    │      API Gateway / BFF            │
    └────┬──────────────────────────────┘
         │
    ┌────▼────────────────────────────────────────────┐
    │                                                  │
    │  ┌──────────────┐  ┌──────────────┐           │
    │  │ Core Service │  │ Realtime     │           │
    │  │ (REST API)   │  │ (WebSocket)  │           │
    │  └──────┬───────┘  └──────┬───────┘           │
    │         │                  │                    │
    │  ┌──────▼─────────────────▼───────┐           │
    │  │      Message Broker (Kafka)    │           │
    │  └──────┬─────────────────┬───────┘           │
    │         │                  │                    │
    │  ┌──────▼──────┐  ┌───────▼───────┐           │
    │  │ Notification│  │  Reporting    │           │
    │  │  Service    │  │   Service     │           │
    │  └─────────────┘  └───────────────┘           │
    └──────────────────────────────────────────────┘
                         │
              ┌──────────┴───────────┐
              │                      │
         ┌────▼────┐           ┌────▼────┐
         │ Oracle  │           │  MinIO  │
         │   DB    │           │ Storage │
         └─────────┘           └─────────┘
```

## 📦 Services

### ✅ 1. Chat Core Service (Phase 1 - COMPLETED)
REST API for conversation and message management.

**Features:**
- Conversation lifecycle (create, assign, transfer, close)
- Message handling with read tracking
- Context capture
- Agent assignment & handover
- Audit logging
- Outbox pattern for events

**Tech:** Spring Boot 4, Oracle DB, Kafka (Outbox)

📁 **Location:** `backend/chat-core-service/`  
📖 **Docs:** [README](backend/chat-core-service/README.md)

### 🔄 2. Chat Realtime Service (Planned)
WebSocket service for real-time messaging.

**Features:**
- WebSocket connections
- Real-time message delivery
- Typing indicators
- Online status
- Push notifications

**Tech:** Spring WebFlux, Redis, STOMP

📁 **Location:** `backend/chat-realtime-service/`

### 📧 3. Chat Notification Service (Planned)
Handles notifications via multiple channels.

**Features:**
- Email notifications
- SMS notifications
- Push notifications (FCM)
- Notification templates
- Delivery tracking

**Tech:** Spring Boot, Kafka Consumer, SendGrid/Twilio

📁 **Location:** `backend/chat-notification-service/`

### 📊 4. Chat Reporting Service (Planned)
Analytics and reporting.

**Features:**
- Conversation analytics
- Agent performance metrics
- SLA monitoring
- Dashboard data aggregation
- Export reports

**Tech:** Spring Boot, Oracle/MongoDB, ElasticSearch

📁 **Location:** `backend/chat-reporting-service/`

## 🚀 Getting Started

### Prerequisites
- JDK 21+
- Maven 3.9+
- Oracle Database 21c
- Docker & Docker Compose (for infrastructure)

### Quick Start - Core Service

1. **Setup Database**
```bash
# Run Oracle setup script
sqlplus system/123@localhost:1521/XEPDB1
@backend/chat-core-service/scripts/setup-oracle-db.sql
```

2. **Build & Run**
```bash
cd backend/chat-core-service
mvn clean install
mvn spring-boot:run
```

3. **Test APIs**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Or use Postman
# Import: postman/Chat-App-API.postman_collection.json
```

## 📚 Documentation

- [Chat Core Service](backend/chat-core-service/README.md)
- [Postman Collection Guide](postman/README.md)
- [Test Guide](postman/TEST_GUIDE.md)

## 🧪 Testing

### Automated Tests
```powershell
cd backend/chat-core-service/scripts
.\test-all-apis.ps1
```

### Postman Collection
See [Postman README](postman/README.md)

## 🗄️ Data Model

### Core Entities
- **Conversation** - Chat session between user and agent
- **Message** - Individual messages with sequence
- **Context** - User context snapshot
- **Assignment** - Agent assignment history
- **Audit Log** - Change tracking

### Event-Driven
- Uses Outbox pattern for reliable event publishing
- Events published to Kafka for other services

## ⚙️ Configuration

### Environment Variables
```bash
# Database
DB_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
DB_USERNAME=CHAT_APP
DB_PASSWORD=123

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# MinIO
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

## 🔐 Security

**Current (Development):**
- Security disabled for easy testing
- All endpoints accessible

**Planned (Production):**
- OAuth2 JWT authentication
- Role-based access control (RBAC)
- API rate limiting
- Data encryption at rest

## 📊 Monitoring

**Available:**
- Actuator health checks
- Prometheus metrics endpoint
- Application logging (Logback)

**Planned:**
- Grafana dashboards
- ELK stack for log aggregation
- Distributed tracing (OpenTelemetry)

## 🎯 Roadmap

### Phase 1 - Core Service ✅ COMPLETED
- [x] REST API endpoints
- [x] Oracle DB integration
- [x] Outbox pattern
- [x] Audit logging
- [x] Postman collection

### Phase 2 - Infrastructure (In Progress)
- [ ] Docker Compose setup
- [ ] Kafka integration
- [ ] MinIO setup
- [ ] Redis caching

### Phase 3 - Realtime Service
- [ ] WebSocket implementation
- [ ] STOMP messaging
- [ ] Real-time notifications

### Phase 4 - Additional Services
- [ ] Notification service
- [ ] Reporting service
- [ ] API Gateway

### Phase 5 - Production Ready
- [ ] Unit & integration tests
- [ ] Performance testing
- [ ] Security hardening
- [ ] CI/CD pipeline

## 🤝 Contributing

Internal project - contact team lead for contribution guidelines.

## 📝 License

Proprietary - Internal Use Only

## 👥 Team

**Backend Team** - Phase 1 MVP Development

---

**Current Version:** Phase 1 Complete  
**Last Updated:** September 2026

# Chat_App