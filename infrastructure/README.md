# Chat App - Infrastructure Setup

Hướng dẫn setup infrastructure cho local development.

## Yêu cầu

- Docker Desktop (Windows/Mac) hoặc Docker Engine + Docker Compose (Linux)
- RAM tối thiểu: 8GB (khuyến nghị 16GB)
- Disk: ~10GB cho tất cả images và volumes

## Quick Start

### 1. Khởi động tất cả services

```powershell
# Từ thư mục gốc chat-app
docker-compose up -d
```

### 2. Kiểm tra trạng thái

```powershell
docker-compose ps
```

Tất cả services phải ở trạng thái `healthy` hoặc `running`.

### 3. Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Oracle Database | `localhost:1521/XE` | system/123 |
| Kafka | `localhost:9092` | - |
| Kafka UI | http://localhost:8090 | - |
| Redis | `localhost:6379` | - |
| MinIO API | http://localhost:9000 | minioadmin/minioadmin |
| MinIO Console | http://localhost:9001 | minioadmin/minioadmin |
| Keycloak | http://localhost:8081 | admin/admin |

## Service Details

### Oracle Database XE 21c

- **Port**: 1521
- **SID**: XE
- **PDB**: XEPDB1
- **User**: system
- **Password**: 123

**Connection String**:
```
jdbc:oracle:thin:@localhost:1521/XEPDB1
```

**Health check**: Có thể mất 2-3 phút để Oracle fully ready sau lần start đầu tiên.

### Kafka + Zookeeper

- **Kafka Bootstrap Server**: `localhost:9092`
- **Zookeeper**: `localhost:2181`
- **Kafka UI**: http://localhost:8090

**Topics được auto-create** khi application publish event lần đầu.

**Kafka UI** giúp monitor topics, messages, consumer groups.

### Redis

- **Port**: 6379
- **Mode**: Standalone với AOF persistence
- **No password**

**Test connection**:
```powershell
docker exec -it chat-redis redis-cli ping
# Kết quả: PONG
```

### MinIO (S3-compatible)

- **API**: http://localhost:9000
- **Console**: http://localhost:9001
- **Access Key**: minioadmin
- **Secret Key**: minioadmin
- **Bucket**: `chat-attachments` (auto-created)

**MinIO Console** cho phép browse và manage objects qua web UI.

### Keycloak (OAuth2/OIDC Provider)

- **URL**: http://localhost:8081
- **Admin Console**: http://localhost:8081/admin
- **Username**: admin
- **Password**: admin

## Setup Keycloak Realm

### Manual Setup (qua Admin Console)

1. Truy cập http://localhost:8081/admin
2. Login với `admin/admin`
3. Tạo realm mới: **chatapp**
4. Tạo client: **chat-core-service**
   - Client Protocol: `openid-connect`
   - Access Type: `confidential`
   - Valid Redirect URIs: `http://localhost:8080/*`
5. Trong tab **Roles**, tạo roles:
   - `user`
   - `agent`
   - `supervisor`
   - `admin`
6. Tạo test users và assign roles tương ứng

### Export/Import Realm (đề xuất)

Sau khi setup manual lần đầu, export realm config:

```bash
docker exec -it chat-keycloak /opt/keycloak/bin/kc.sh export --dir /tmp/export --realm chatapp
docker cp chat-keycloak:/tmp/export/chatapp-realm.json ./infrastructure/keycloak/
```

Lần sau chỉ cần import:

```bash
docker cp ./infrastructure/keycloak/chatapp-realm.json chat-keycloak:/tmp/
docker exec -it chat-keycloak /opt/keycloak/bin/kc.sh import --file /tmp/chatapp-realm.json
```

## Application Configuration

File `application.yml` đã được config sẵn để kết nối với infrastructure này:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/XEPDB1
    username: system
    password: 123

  kafka:
    bootstrap-servers: localhost:9092

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8081/realms/chatapp

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: chat-attachments
```

## Troubleshooting

### Oracle container không start

- **Nguyên nhân**: Thiếu RAM hoặc disk
- **Giải pháp**: Tăng Docker Desktop resources (Settings → Resources)
- **Hoặc**: Dùng PostgreSQL thay thế (nhẹ hơn)

### Kafka connection refused

- **Check**: Zookeeper phải healthy trước
- **Giải pháp**: 
  ```powershell
  docker-compose restart zookeeper
  docker-compose restart kafka
  ```

### MinIO bucket không tồn tại

- **Check**: `minio-client` container đã chạy xong chưa
- **Manual create**:
  ```powershell
  docker exec -it chat-minio-client mc mb myminio/chat-attachments
  ```

### Keycloak realm not found

- **Check**: Realm `chatapp` đã được tạo chưa
- **Test token**: Dùng Postman/curl để lấy token:
  ```bash
  curl -X POST http://localhost:8081/realms/chatapp/protocol/openid-connect/token \
    -d "client_id=chat-core-service" \
    -d "client_secret=<your-secret>" \
    -d "grant_type=password" \
    -d "username=testuser" \
    -d "password=password"
  ```

## Stop & Clean Up

### Stop tất cả services

```powershell
docker-compose stop
```

### Stop và xóa containers (giữ data)

```powershell
docker-compose down
```

### Xóa hết (bao gồm volumes/data)

```powershell
docker-compose down -v
```

## Production Deployment

⚠️ **Docker Compose này CHỈ cho development!**

Production cần:
- Oracle RAC hoặc PostgreSQL HA cluster
- Kafka cluster (3+ brokers, replication factor ≥ 2)
- Redis Cluster/Sentinel
- MinIO distributed mode hoặc S3
- Keycloak HA với external database
- TLS/SSL cho tất cả connections
- Network policies và security hardening

Tham khảo section 17 của tài liệu thiết kế kiến trúc.
