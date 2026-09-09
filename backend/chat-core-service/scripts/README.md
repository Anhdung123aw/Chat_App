# Chat Core Service - Scripts

Utility scripts for testing and database management.

## 📁 Files

### Testing Scripts

**test-all-apis.ps1**
- Full API integration test
- Tests all conversation and message endpoints
- Usage: `.\test-all-apis.ps1`

### Database Scripts

**VERIFY_DATA.sql**
- Query to verify data in Oracle DB after API calls
- Check conversations, messages, assignments, audit logs, and outbox events
- Run in DBeaver or SQL*Plus

## 🚀 Quick Start

### Test APIs
```powershell
cd scripts
.\test-all-apis.ps1
```

### Verify DB Data
Open `VERIFY_DATA.sql` in DBeaver and execute against CHAT_APP schema.

## 📝 Notes

- Ensure Spring Boot app is running on `localhost:8080`
- Oracle DB should be accessible with schema `CHAT_APP`
- For Postman testing, use collection in `../../../postman/`
