# Quick Fix - Authentication Issue

## Problem
API trả về `401 Unauthorized` khi test trong Postman vì đang require JWT token.

## Solution - Dev Mode (No Auth)

### ✅ Đã fix tự động

1. **Tạo DevSecurityConfig** - Security config cho development
2. **Set profile = dev** trong `application.yml`

### 🔄 Restart Application

```powershell
# Stop app hiện tại (Ctrl+C)
# Restart
cd backend\chat-core-service
mvn spring-boot:run
```

### ✅ Test lại

```bash
POST http://localhost:8080/api/v1/conversations
Body: {
  "userId": "user123",
  "merchantId": "merchant001",
  "topicCode": "ACCOUNT"
}
```

➡️ **Expected**: 201 Created (KHÔNG CẦN Authorization header)

---

## Alternative: Run with specific profile

Nếu không muốn sửa `application.yml`:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Production Mode (With OAuth2)

Khi deploy production, đổi profile:

```yaml
spring:
  profiles:
    active: prod  # Sử dụng SecurityConfig (OAuth2)
```

Hoặc set environment variable:
```bash
SPRING_PROFILES_ACTIVE=prod
```

---

## Verify Profile Active

Check startup logs:
```
The following 1 profile is active: "dev"
```

Hoặc test endpoint:
```
GET http://localhost:8080/actuator/env
```

Tìm `"activeProfiles": ["dev"]`
