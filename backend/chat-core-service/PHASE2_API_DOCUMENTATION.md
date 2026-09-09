# Phase 2 API Documentation

**Chat Application - Operations & Analytics APIs**

Tài liệu này mô tả 45 REST APIs mới được thêm trong Phase 2, bao gồm: Auto Assignment, SLA Monitoring, Canned Replies, Ratings/CSAT, Supervisor Dashboard, và Reporting.

---

## 1. Agent Management APIs (9 APIs)

**Base Path:** `/api/agents`

### 1.1 Create Agent
```http
POST /api/agents
Content-Type: application/json

{
  "agentName": "John Doe",
  "email": "john.doe@company.com",
  "skills": ["BILLING", "PAYMENT", "ACCOUNT"],
  "maxConcurrentChats": 5
}
```

**Response:** `201 Created`
```json
{
  "agentId": "AGT001",
  "agentName": "John Doe",
  "email": "john.doe@company.com",
  "status": "OFFLINE",
  "skills": ["BILLING", "PAYMENT", "ACCOUNT"],
  "maxConcurrentChats": 5,
  "currentChatCount": 0,
  "lastActiveAt": null
}
```

### 1.2 Update Agent Status
```http
PUT /api/agents/{agentId}/status
Content-Type: application/json

{
  "status": "ONLINE"
}
```

**Response:** `200 OK`

### 1.3 Update Agent Capacity
```http
PUT /api/agents/{agentId}/capacity
Content-Type: application/json

{
  "maxConcurrentChats": 8
}
```

**Response:** `200 OK`

### 1.4 Agent Heartbeat
```http
POST /api/agents/{agentId}/heartbeat
```

**Response:** `200 OK`
- Updates `lastActiveAt` timestamp

### 1.5 Get Agent by ID
```http
GET /api/agents/{agentId}
```

**Response:** `200 OK`
```json
{
  "agentId": "AGT001",
  "agentName": "John Doe",
  "status": "ONLINE",
  "skills": ["BILLING", "PAYMENT"],
  "currentChatCount": 3,
  "maxConcurrentChats": 5
}
```

### 1.6 Get Online Agents
```http
GET /api/agents/online
```

**Response:** `200 OK`
```json
{
  "agents": [
    {
      "agentId": "AGT001",
      "agentName": "John Doe",
      "status": "ONLINE",
      "currentChatCount": 3,
      "maxConcurrentChats": 5
    }
  ],
  "totalOnline": 1
}
```

### 1.7 Get Available Agents
```http
GET /api/agents/available
```

**Response:** `200 OK`
- Returns agents with status=ONLINE and currentChatCount < maxConcurrentChats

### 1.8 Get Agents by Skills
```http
GET /api/agents/by-skills?skills=BILLING,PAYMENT
```

**Response:** `200 OK`
```json
{
  "agents": [
    {
      "agentId": "AGT001",
      "skills": ["BILLING", "PAYMENT", "ACCOUNT"]
    }
  ]
}
```

### 1.9 Mark Stale Agents Offline
```http
POST /api/agents/mark-stale-offline?inactiveMinutes=10
```

**Response:** `200 OK`
```json
{
  "markedOfflineCount": 3
}
```

---

## 2. SLA Management APIs (9 APIs)

**Base Path:** `/api/sla`

### 2.1 Create SLA Config
```http
POST /api/sla/configs
Content-Type: application/json

{
  "topicCode": "BILLING",
  "firstResponseTimeSeconds": 300,
  "resolutionTimeSeconds": 3600,
  "enabled": true
}
```

**Response:** `201 Created`

### 2.2 Update SLA Config
```http
PUT /api/sla/configs/{configId}
Content-Type: application/json

{
  "firstResponseTimeSeconds": 180,
  "resolutionTimeSeconds": 1800
}
```

**Response:** `200 OK`

### 2.3 Get SLA Config by ID
```http
GET /api/sla/configs/{configId}
```

**Response:** `200 OK`
```json
{
  "configId": "SLA001",
  "topicCode": "BILLING",
  "firstResponseTimeSeconds": 300,
  "resolutionTimeSeconds": 3600,
  "enabled": true
}
```

### 2.4 Get All SLA Configs
```http
GET /api/sla/configs
```

**Response:** `200 OK`

### 2.5 Get SLA Violations
```http
GET /api/sla/violations
```

**Response:** `200 OK`
```json
{
  "violations": [
    {
      "conversationId": "CONV001",
      "customerId": "CUST001",
      "assignedAgent": "AGT001",
      "slaDeadline": "2026-09-07T10:05:00",
      "minutesOverdue": 15,
      "topicCode": "BILLING"
    }
  ],
  "totalViolations": 1
}
```

### 2.6 Get At-Risk Conversations
```http
GET /api/sla/at-risk?withinMinutes=5
```

**Response:** `200 OK`
```json
{
  "atRiskConversations": [
    {
      "conversationId": "CONV002",
      "slaDeadline": "2026-09-07T10:13:00",
      "minutesRemaining": 3
    }
  ]
}
```

### 2.7 Get SLA Statistics
```http
GET /api/sla/statistics?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "startDate": "2026-09-01T00:00:00",
  "endDate": "2026-09-07T23:59:59",
  "totalConversations": 100,
  "metSla": 85,
  "breachedSla": 15,
  "complianceRate": 85.0
}
```

### 2.8 Check Conversation SLA
```http
GET /api/sla/conversations/{conversationId}/check
```

**Response:** `200 OK`
```json
{
  "conversationId": "CONV001",
  "slaDeadline": "2026-09-07T10:05:00",
  "firstResponseAt": "2026-09-07T10:03:00",
  "isMet": true,
  "minutesRemaining": 2
}
```

### 2.9 Get SLA Config by Topic
```http
GET /api/sla/configs/by-topic/{topicCode}
```

**Response:** `200 OK`

---

## 3. Canned Reply APIs (6 APIs)

**Base Path:** `/api/canned-replies`

### 3.1 Create Canned Reply
```http
POST /api/canned-replies
Content-Type: application/json

{
  "category": "GREETING",
  "shortcut": "/hi",
  "title": "Welcome Message",
  "content": "Xin chào! Tôi có thể giúp gì cho bạn?",
  "language": "vi"
}
```

**Response:** `201 Created`

### 3.2 Update Canned Reply
```http
PUT /api/canned-replies/{replyId}
Content-Type: application/json

{
  "content": "Xin chào! Chúng tôi có thể hỗ trợ gì cho bạn?",
  "enabled": true
}
```

**Response:** `200 OK`

### 3.3 Get Canned Reply by ID
```http
GET /api/canned-replies/{replyId}
```

**Response:** `200 OK`

### 3.4 Get All Canned Replies
```http
GET /api/canned-replies
```

**Response:** `200 OK`
```json
{
  "replies": [
    {
      "replyId": "RPL001",
      "category": "GREETING",
      "shortcut": "/hi",
      "title": "Welcome Message",
      "content": "Xin chào! Tôi có thể giúp gì cho bạn?",
      "enabled": true
    }
  ]
}
```

### 3.5 Search by Shortcut
```http
GET /api/canned-replies/search/by-shortcut?shortcut=/hi
```

**Response:** `200 OK`

### 3.6 Get by Category
```http
GET /api/canned-replies/by-category/{category}
```

**Response:** `200 OK`

---

## 4. Rating/CSAT APIs (4 APIs)

**Base Path:** `/api/ratings`

### 4.1 Submit Rating
```http
POST /api/ratings
Content-Type: application/json

{
  "conversationId": "CONV001",
  "rating": 5,
  "comment": "Excellent support! Very helpful.",
  "customerId": "CUST001"
}
```

**Response:** `201 Created`
```json
{
  "ratingId": "RAT001",
  "conversationId": "CONV001",
  "rating": 5,
  "comment": "Excellent support! Very helpful.",
  "createdAt": "2026-09-07T10:30:00"
}
```

### 4.2 Get Rating by ID
```http
GET /api/ratings/{ratingId}
```

**Response:** `200 OK`

### 4.3 Get CSAT Score
```http
GET /api/ratings/csat/score?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "startDate": "2026-09-01T00:00:00",
  "endDate": "2026-09-07T23:59:59",
  "averageRating": 4.5,
  "totalRatings": 80,
  "csatScore": 90.0,
  "distribution": {
    "1": 2,
    "2": 3,
    "3": 10,
    "4": 25,
    "5": 40
  }
}
```

### 4.4 Get CSAT Report
```http
GET /api/ratings/csat/report?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`

---

## 5. Supervisor Dashboard APIs (9 APIs)

**Base Path:** `/api/supervisor`

### 5.1 Get Dashboard Overview
```http
GET /api/supervisor/dashboard
```

**Response:** `200 OK`
```json
{
  "timestamp": "2026-09-07T10:00:00",
  "agentMetrics": {
    "totalAgents": 15,
    "onlineAgents": 12,
    "busyAgents": 8,
    "availableAgents": 4
  },
  "queueMetrics": {
    "queuedConversations": 25,
    "averageWaitTimeMinutes": 8.5,
    "longestWaitTimeMinutes": 20.0
  },
  "slaMetrics": {
    "activeConversations": 50,
    "atRisk": 5,
    "violated": 2,
    "complianceRate": 96.0
  },
  "csatScore": 4.5
}
```

### 5.2 Get Queue Details
```http
GET /api/supervisor/queue
```

**Response:** `200 OK`
```json
{
  "queuedConversations": [
    {
      "conversationId": "CONV001",
      "customerId": "CUST001",
      "topicCode": "BILLING",
      "waitTimeMinutes": 12,
      "priority": "HIGH"
    }
  ],
  "totalQueued": 25
}
```

### 5.3 Get SLA Violations
```http
GET /api/supervisor/sla/violations
```

**Response:** `200 OK`

### 5.4 Get Performance Metrics
```http
GET /api/supervisor/performance?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-09-07T23:59:59"
  },
  "metrics": {
    "totalConversations": 500,
    "closedConversations": 450,
    "averageFrtMinutes": 3.5,
    "averageAhtMinutes": 15.2,
    "slaComplianceRate": 92.0,
    "csatScore": 4.5
  }
}
```

### 5.5 Get Agent Details
```http
GET /api/supervisor/agents/{agentId}
```

**Response:** `200 OK`
```json
{
  "agentId": "AGT001",
  "agentName": "John Doe",
  "status": "ONLINE",
  "currentChats": [
    {
      "conversationId": "CONV001",
      "customerId": "CUST001",
      "duration": 15
    }
  ],
  "todayMetrics": {
    "conversationsHandled": 20,
    "averageHandleTime": 12.5,
    "csatScore": 4.8
  }
}
```

### 5.6 Get Team Performance
```http
GET /api/supervisor/team/performance?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`

### 5.7 Batch Assign Conversations
```http
POST /api/supervisor/batch-assign
Content-Type: application/json

{
  "conversationIds": ["CONV001", "CONV002", "CONV003"],
  "targetAgentId": "AGT001"
}
```

**Response:** `200 OK`
```json
{
  "assignedCount": 3,
  "failedAssignments": []
}
```

### 5.8 Reassign Conversation
```http
POST /api/supervisor/conversations/{conversationId}/reassign
Content-Type: application/json

{
  "fromAgentId": "AGT001",
  "toAgentId": "AGT002",
  "reason": "Load balancing"
}
```

**Response:** `200 OK`

### 5.9 Check Alerts
```http
GET /api/supervisor/alerts
```

**Response:** `200 OK`
```json
{
  "alerts": [
    {
      "alertType": "QUEUE_DEPTH",
      "severity": "HIGH",
      "message": "Queue depth (35) exceeded threshold (30)",
      "timestamp": "2026-09-07T10:00:00"
    },
    {
      "alertType": "SLA_BREACH",
      "severity": "CRITICAL",
      "message": "SLA breach rate (25%) exceeded threshold (20%)",
      "timestamp": "2026-09-07T09:55:00"
    }
  ],
  "totalAlerts": 2
}
```

---

## 6. Reporting APIs (8 APIs)

**Base Path:** `/api/reports`

### 6.1 Get First Response Time Report
```http
GET /api/reports/frt?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-09-07T23:59:59"
  },
  "averageFrtMinutes": 3.5,
  "medianFrtMinutes": 3.0,
  "percentile95Minutes": 8.0,
  "totalConversations": 500,
  "distribution": {
    "0-2min": 200,
    "2-5min": 250,
    "5-10min": 40,
    "10+min": 10
  }
}
```

### 6.2 Get Average Handle Time Report
```http
GET /api/reports/aht?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-09-07T23:59:59"
  },
  "averageAhtMinutes": 15.2,
  "medianAhtMinutes": 12.0,
  "totalConversations": 450
}
```

### 6.3 Get CSAT Report
```http
GET /api/reports/csat?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-09-07T23:59:59"
  },
  "averageScore": 4.5,
  "csatPercentage": 90.0,
  "totalRatings": 400,
  "responseRate": 88.9,
  "trend": {
    "week1": 4.3,
    "week2": 4.5,
    "week3": 4.6,
    "week4": 4.7
  }
}
```

### 6.4 Get SLA Breach Report
```http
GET /api/reports/sla-breach?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-09-07T23:59:59"
  },
  "totalConversations": 500,
  "breachedConversations": 40,
  "complianceRate": 92.0,
  "breachByTopic": {
    "BILLING": 15,
    "TECHNICAL": 20,
    "ACCOUNT": 5
  }
}
```

### 6.5 Get Top Topics Report
```http
GET /api/reports/top-topics?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59&limit=10
```

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-09-07T23:59:59"
  },
  "topTopics": [
    {
      "topicCode": "BILLING",
      "conversationCount": 200,
      "percentage": 40.0,
      "averageFrtMinutes": 3.2,
      "averageAhtMinutes": 12.5,
      "csatScore": 4.6
    },
    {
      "topicCode": "TECHNICAL",
      "conversationCount": 150,
      "percentage": 30.0,
      "averageFrtMinutes": 4.5,
      "averageAhtMinutes": 20.0,
      "csatScore": 4.2
    }
  ]
}
```

### 6.6 Get Agent Performance Report
```http
GET /api/reports/agent-performance?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-09-07T23:59:59"
  },
  "agentPerformance": [
    {
      "agentId": "AGT001",
      "agentName": "John Doe",
      "conversationsHandled": 120,
      "averageFrtMinutes": 2.8,
      "averageAhtMinutes": 11.5,
      "csatScore": 4.8,
      "slaComplianceRate": 95.0
    }
  ]
}
```

### 6.7 Get Backlog Report
```http
GET /api/reports/backlog
```

**Response:** `200 OK`
```json
{
  "timestamp": "2026-09-07T10:00:00",
  "queuedConversations": 25,
  "averageWaitMinutes": 8.5,
  "longestWaitMinutes": 20.0,
  "queueByTopic": {
    "BILLING": 10,
    "TECHNICAL": 12,
    "ACCOUNT": 3
  },
  "queueByPriority": {
    "HIGH": 5,
    "MEDIUM": 15,
    "LOW": 5
  }
}
```

### 6.8 Get Comprehensive Dashboard
```http
GET /api/reports/dashboard?startDate=2026-09-01T00:00:00&endDate=2026-09-07T23:59:59
```

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2026-09-01T00:00:00",
    "endDate": "2026-09-07T23:59:59"
  },
  "summary": {
    "totalConversations": 500,
    "closedConversations": 450,
    "averageFrtMinutes": 3.5,
    "averageAhtMinutes": 15.2,
    "csatScore": 4.5,
    "slaComplianceRate": 92.0
  },
  "trends": {
    "conversationsPerDay": [70, 75, 68, 72, 80, 65, 70],
    "csatPerDay": [4.3, 4.5, 4.6, 4.4, 4.7, 4.5, 4.6]
  },
  "topIssues": [
    {
      "topicCode": "BILLING",
      "count": 200
    }
  ],
  "alerts": [
    {
      "type": "SLA_BREACH",
      "count": 2
    }
  ]
}
```

---

## Error Responses

All APIs follow standard HTTP error codes:

### 400 Bad Request
```json
{
  "timestamp": "2026-09-07T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input: rating must be between 1 and 5",
  "path": "/api/ratings"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-09-07T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Agent not found with ID: AGT999",
  "path": "/api/agents/AGT999"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2026-09-07T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/reports/dashboard"
}
```

---

## Authentication & Authorization

All Phase 2 APIs require authentication via JWT token in the `Authorization` header:

```http
Authorization: Bearer <jwt_token>
```

### Role-Based Access Control (RBAC)

| Role | Access |
|------|--------|
| **AGENT** | Agent management (self), Canned replies (read), Submit ratings |
| **SUPERVISOR** | All Agent APIs, All Supervisor APIs, All Report APIs |
| **ADMIN** | Full access to all APIs |

---

## Rate Limiting

All APIs are rate-limited to prevent abuse:

- **Agent APIs:** 100 requests/minute per agent
- **Report APIs:** 10 requests/minute per user
- **Supervisor APIs:** 50 requests/minute per supervisor

Rate limit headers:
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1694088000
```

---

## Webhooks & Events

Phase 2 supports webhook notifications for critical events:

### Supported Events

1. **sla.violated** - Fired when SLA deadline is breached
2. **queue.threshold.exceeded** - Fired when queue depth exceeds threshold
3. **agent.capacity.low** - Fired when <30% agents available
4. **rating.received** - Fired when customer submits rating

### Webhook Payload Example
```json
{
  "event": "sla.violated",
  "timestamp": "2026-09-07T10:15:00",
  "data": {
    "conversationId": "CONV001",
    "customerId": "CUST001",
    "assignedAgent": "AGT001",
    "minutesOverdue": 15
  }
}
```

---

## Best Practices

1. **Pagination:** Use `page` and `size` query parameters for list endpoints
2. **Filtering:** Use query parameters for filtering (e.g., `?status=ONLINE`)
3. **Date Ranges:** Always provide `startDate` and `endDate` for report APIs
4. **Caching:** Report APIs cache responses for 5 minutes
5. **Batch Operations:** Use batch APIs when updating multiple resources

---

## Change Log

### Phase 2 - v2.0.0 (2026-09-07)
- ✅ Added 45 new REST APIs
- ✅ Auto Assignment engine
- ✅ SLA monitoring & alerting
- ✅ Canned reply management
- ✅ CSAT ratings & reporting
- ✅ Supervisor dashboard
- ✅ Comprehensive analytics & reports

---

**For technical support, contact:** dev-team@company.com
