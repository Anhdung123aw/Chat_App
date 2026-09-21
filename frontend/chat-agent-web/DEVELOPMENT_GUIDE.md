# Chat Agent Web - Development Guide

## 🎯 Project Overview

Angular 18+ Chat Application với real-time WebSocket communication.

## 📁 Project Structure

```
src/app/
├── core/                           # Core module
│   ├── models/                    # TypeScript interfaces
│   │   ├── user.model.ts         ✅ Done
│   │   ├── conversation.model.ts ✅ Done
│   │   ├── message.model.ts      ✅ Done
│   │   └── websocket.model.ts    ✅ Done
│   ├── services/                  # Singleton services
│   │   ├── websocket.service.ts  ✅ Done - WebSocket + STOMP
│   │   ├── api.service.ts        ✅ Done - REST API
│   │   └── auth.service.ts       ✅ Done - Authentication
│   └── interceptors/              
│       └── auth.interceptor.ts   ✅ Done - JWT token
│
├── features/                       
│   ├── auth/                      
│   │   └── login/                ✅ Done - Login component
│   │
│   ├── agent/                     # Agent Console
│   │   ├── agent-layout/         ✅ Done - Main layout
│   │   ├── conversation-list/    ⏳ TODO
│   │   ├── chat/                 ⏳ TODO
│   │   └── canned-replies/       ⏳ TODO
│   │
│   └── supervisor/                # Supervisor Dashboard
│       ├── supervisor-layout/    ⏳ TODO
│       ├── overview/             ⏳ TODO
│       ├── agent-list/           ⏳ TODO
│       ├── queue/                ⏳ TODO
│       └── reports/              ⏳ TODO
│
└── shared/                        # Shared components
    └── components/                ⏳ TODO
```

## 🚀 Getting Started

### 1. Install Dependencies
```bash
cd frontend/chat-agent-web
npm install
```

### 2. Run Development Server
```bash
ng serve
# Access: http://localhost:4200
```

### 3. Login Credentials (Demo)
- **Agent**: username=`agent1`, password=`password`
- **Supervisor**: username=`supervisor1`, password=`password`

## 📝 Components TODO

### Agent Console Components

#### 1. Conversation List Component (`conversation-list.component.ts`)
**Location**: `src/app/features/agent/conversation-list/`

**Features**:
- List active conversations
- Filter by status (PENDING, ASSIGNED, ACTIVE)
- Search by customer name
- Real-time updates via WebSocket
- Badge showing unread count

**Implementation**:
```typescript
// Key methods:
- loadConversations(): Load via apiService.getAgentConversations()
- subscribeToUpdates(): Listen WebSocket for new assignments
- selectConversation(id): Navigate to /agent/chat/:id
- acceptConversation(id): Assign pending conversation
```

**Template Structure**:
- Header với search box
- Filter tabs (All/Active/Pending)
- List items với:
  - Customer name + avatar
  - Last message preview
  - Timestamp
  - Unread badge
  - Priority indicator

---

#### 2. Chat Component (`chat.component.ts`)
**Location**: `src/app/features/agent/chat/`

**Features**:
- Display message history
- Real-time message receiving via WebSocket
- Send messages (text, file)
- Typing indicator
- Customer info sidebar
- Canned replies quick access

**Implementation**:
```typescript
// Key methods:
- loadMessages(): apiService.getConversationMessages(conversationId)
- subscribeToConversation(): wsService.subscribeToConversation(conversationId)
- sendMessage(content): apiService.sendMessage({ conversationId, content })
- onTyping(): wsService.sendTypingIndicator(conversationId, true)
- closeConversation(): apiService.closeConversation(conversationId)
```

**Template Structure**:
- Header: Customer name, status, actions (close, transfer)
- Message area: Scroll list với auto-scroll to bottom
- Typing indicator: "Customer is typing..."
- Input area: Text box + send button + attach button
- Sidebar: Customer context panel

---

#### 3. Canned Reply Component (`canned-replies.component.ts`)
**Location**: `src/app/features/agent/canned-replies/`

**Features**:
- Quick reply templates
- Search/filter by category
- Insert to chat input
- Keyboard shortcuts

**Implementation**:
```typescript
// Key methods:
- loadCannedReplies(): apiService.getCannedReplies()
- filterByCategory(category): Filter list
- selectReply(reply): Emit selected reply
```

---

### Supervisor Dashboard Components

#### 1. Supervisor Layout (`supervisor-layout.component.ts`)
**Location**: `src/app/features/supervisor/supervisor-layout/`

Similar to Agent Layout but với navigation items:
- Overview (dashboard stats)
- Agents (agent list + performance)
- Queue (pending conversations)
- Reports (analytics)

---

#### 2. Overview Component (`overview.component.ts`)
**Location**: `src/app/features/supervisor/overview/`

**Features**:
- Real-time dashboard statistics
- Active conversations count
- Average waiting time
- Agent status summary (online/busy/offline)
- Charts (if using charting library)

**Data từ API**:
- `apiService.getDashboardStats()`
- `apiService.getQueueStats()`

---

#### 3. Agent List Component (`agent-list.component.ts`)
**Location**: `src/app/features/supervisor/agent-list/`

**Features**:
- List all agents
- Presence status (online/offline/away)
- Active conversations count
- Performance metrics
- Real-time updates

**Implementation**:
```typescript
// Key methods:
- loadAgents(): apiService.getAllAgents()
- loadPresence(): apiService.getBulkPresence(agentIds)
- subscribeToPresenceUpdates(): Listen WebSocket presence events
```

---

#### 4. Queue Component (`queue.component.ts`)
**Location**: `src/app/features/supervisor/queue/`

**Features**:
- List pending conversations
- Waiting time for each
- Priority sorting
- Manual assignment to agent

**Implementation**:
```typescript
// Key methods:
- loadPendingConversations(): apiService.getPendingConversations()
- assignToAgent(conversationId, agentId): apiService.assignConversation()
```

---

## 🎨 Styling Guide

### Global Styles (`styles.scss`)

```scss
// Variables
$primary-color: #667eea;
$secondary-color: #764ba2;
$success-color: #2ecc71;
$danger-color: #e74c3c;
$warning-color: #f39c12;

// Typography
$font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

// Spacing
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;

// Reset & Base
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: $font-family;
  font-size: 14px;
  color: #333;
  background: #f5f5f5;
}

// Utility classes
.text-muted { color: #999; }
.text-small { font-size: 12px; }
.text-center { text-align: center; }

// Button styles
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;

  &-primary {
    background: linear-gradient(135deg, $primary-color 0%, $secondary-color 100%);
    color: white;

    &:hover { transform: translateY(-2px); }
  }

  &-success {
    background: $success-color;
    color: white;
  }

  &-danger {
    background: $danger-color;
    color: white;
  }
}

// Card
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}
```

---

## 🔌 WebSocket Integration

### Connecting
```typescript
// In login.component.ts (already implemented)
this.wsService.connect(userId, userType);
```

### Subscribe to Conversation
```typescript
// In chat.component.ts
this.wsService.subscribeToConversation(conversationId).subscribe(message => {
  if (message.type === WebSocketMessageType.MESSAGE) {
    // New message received
    this.messages.push(message.payload);
  }
});
```

### Send Typing Indicator
```typescript
// On textarea input event
onInputChange() {
  this.wsService.sendTypingIndicator(this.conversationId, true);
  
  // Stop after 2 seconds
  clearTimeout(this.typingTimeout);
  this.typingTimeout = setTimeout(() => {
    this.wsService.sendTypingIndicator(this.conversationId, false);
  }, 2000);
}
```

---

## 🧪 Testing

### Run Tests
```bash
ng test
```

### Run E2E Tests
```bash
ng e2e
```

---

## 📦 Build for Production

```bash
ng build --configuration production
# Output: dist/chat-agent-web/
```

---

## 🔧 Backend Integration

### Backend Services Required:
1. **chat-core-service** (port 8081)
   - Authentication: POST /api/auth/login
   - Conversations: GET /api/conversations/*
   - Messages: GET/POST /api/messages/*
   - Agents: GET /api/agents/*

2. **chat-realtime-service** (port 8083)
   - WebSocket: ws://localhost:8083/ws
   - Presence API: GET/PUT /api/presence/*

### Start Backend Services:
```bash
# Terminal 1 - Core Service
cd backend/chat-core-service
mvn spring-boot:run

# Terminal 2 - Realtime Service
cd backend/chat-realtime-service
mvn spring-boot:run
```

---

## 📚 Additional Libraries (Optional)

### Charts (for Supervisor Dashboard)
```bash
npm install chart.js ng2-charts
```

### Date/Time Formatting
```bash
npm install date-fns
```

### Icons
```bash
npm install @ng-icons/core @ng-icons/heroicons
```

---

## 🐛 Common Issues

### WebSocket không connect
- Check backend realtime-service đang chạy
- Check CORS configuration trong backend
- Check browser console for errors

### Messages không real-time
- Verify WebSocket connection status
- Check subscription to correct conversation topic
- Check browser DevTools Network tab (WS filter)

### Authentication errors
- Verify token trong localStorage
- Check HTTP interceptor đang add header
- Verify backend JWT configuration

---

## 📖 Next Steps

1. Generate remaining components bằng Angular CLI:
```bash
ng generate component features/agent/conversation-list
ng generate component features/agent/chat
ng generate component features/supervisor/overview
# etc...
```

2. Implement business logic theo guide trên

3. Add styling với SCSS

4. Test integration với backend

5. Add error handling và loading states

6. Polish UI/UX

---

## 🎓 Angular Best Practices

- ✅ Use standalone components (already configured)
- ✅ Lazy load feature modules via routing
- ✅ Use services for business logic
- ✅ Use RxJS for async operations
- ✅ Unsubscribe from observables in ngOnDestroy
- ✅ Use OnPush change detection strategy when possible
- ✅ Keep components small and focused
- ✅ Use TypeScript strict mode

---

## 🤝 Need Help?

- Angular Docs: https://angular.dev
- STOMP.js Docs: https://stomp-js.github.io/
- RxJS Docs: https://rxjs.dev

---

**Happy Coding! 🚀**
