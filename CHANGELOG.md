# Changelog

All notable changes to this project will be documented in this file.

## [Phase 1 MVP] - 2026-09-07

### ✅ Completed Features

#### Chat Core Service
- **REST API Endpoints**
  - Conversation management (CRUD operations)
  - Message sending and retrieval
  - Agent assignment and transfer
  - Conversation closure workflow
  - Read/unread message tracking
  
- **Database Integration**
  - Oracle 21c database setup
  - 8 core tables (Conversation, Message, Context, Attachment, Outbox, Assignment, Audit, Flyway)
  - JPA entities with optimistic locking
  - Flyway migrations
  
- **Event-Driven Architecture**
  - Outbox pattern implementation
  - Event publishing for Kafka integration
  - Audit logging with AOP
  
- **Context Management**
  - User context capture (screen, device, app version)
  - Context validation (PII filtering)
  - Context storage per conversation
  
- **Attachment Support**
  - Metadata storage for MinIO integration
  - Presigned URL generation
  - File type and size validation

#### Testing
- Postman collection with auto variable management
- PowerShell automated test scripts
- Database verification queries
- Complete test documentation

#### Documentation
- Project README with architecture overview
- Service-level documentation
- API usage examples
- Setup and troubleshooting guides

### 🔧 Technical Changes

- **Java**: JDK 21 with Spring Boot 4.1.1
- **Database**: Oracle 21c with CHAT_APP schema
- **Build**: Maven 3.9+
- **Logging**: Structured logging with Logback
- **Monitoring**: Actuator endpoints with Prometheus metrics

### 🐛 Bug Fixes

- Fixed Oracle TIMESTAMP mapping issue (changed from Instant to LocalDateTime)
- Fixed Spring Security 403 Forbidden (disabled method-level security for dev)
- Fixed Lombok runtime issues (removed @SneakyThrows)
- Fixed Flyway migration conflicts

### 📝 Known Issues

- Kafka publisher not yet implemented (events stored in CHAT_OUTBOX)
- MinIO integration pending (only metadata storage working)
- Security disabled for development (OAuth2 JWT not configured)
- No unit tests yet

### 🚀 Next Phase (Phase 2)

- [ ] Docker Compose infrastructure setup
- [ ] Kafka integration and OutboxPublisher implementation
- [ ] MinIO file upload/download
- [ ] Redis caching layer
- [ ] Unit and integration tests

---

## Version History

- **v0.0.1-SNAPSHOT** - Phase 1 MVP Complete (2026-09-07)
