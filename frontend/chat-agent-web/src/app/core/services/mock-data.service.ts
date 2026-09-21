import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MockDataService {
  
  private mockAgents = [
    { agentId: 'agent-001', agentName: 'John Doe', email: 'john@example.com', status: 'ONLINE', skills: '["ORDER_SUPPORT","PAYMENT"]', currentChatCount: 2, maxConcurrentChats: 5 },
    { agentId: 'agent-002', agentName: 'Jane Smith', email: 'jane@example.com', status: 'ONLINE', skills: '["PAYMENT","ACCOUNT"]', currentChatCount: 1, maxConcurrentChats: 5 },
    { agentId: 'agent-003', agentName: 'Bob Wilson', email: 'bob@example.com', status: 'AWAY', skills: '["ORDER_SUPPORT"]', currentChatCount: 0, maxConcurrentChats: 5 }
  ];

  private mockConversations = [
    {
      id: 'conv-001',
      conversationId: 'conv-001',
      customerId: 'cust-001',
      customerName: 'Alice Johnson',
      agentId: 'agent-001',
      agentName: 'John Doe',
      status: 'ACTIVE',
      priority: 'NORMAL',
      topicCode: 'ORDER_SUPPORT',
      channel: 'WEB',
      assignedAgentId: 'agent-001',
      createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(), // 30 mins ago
      updatedAt: new Date(Date.now() - 1000 * 60 * 2).toISOString(),
      lastMessageAt: new Date(Date.now() - 1000 * 60 * 2).toISOString(),
      unreadCount: 2
    },
    {
      id: 'conv-002',
      conversationId: 'conv-002',
      customerId: 'cust-002',
      customerName: 'Bob Smith',
      agentId: 'agent-001',
      agentName: 'John Doe',
      status: 'ACTIVE',
      priority: 'NORMAL',
      topicCode: 'PAYMENT',
      channel: 'WEB',
      assignedAgentId: 'agent-001',
      createdAt: new Date(Date.now() - 1000 * 60 * 60).toISOString(), // 1 hour ago
      updatedAt: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
      lastMessageAt: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
      unreadCount: 0
    },
    {
      id: 'conv-003',
      conversationId: 'conv-003',
      customerId: 'cust-003',
      customerName: 'Charlie Brown',
      agentId: null,
      agentName: null,
      status: 'WAITING',
      priority: 'HIGH',
      topicCode: 'ORDER_SUPPORT',
      channel: 'WEB',
      assignedAgentId: null,
      createdAt: new Date(Date.now() - 1000 * 60 * 10).toISOString(),
      updatedAt: new Date(Date.now() - 1000 * 60 * 10).toISOString(),
      lastMessageAt: new Date(Date.now() - 1000 * 60 * 10).toISOString(),
      unreadCount: 1
    }
  ];

  private mockMessages: any = {
    'conv-001': [
      { id: 'msg-001', messageId: 'msg-001', conversationId: 'conv-001', content: 'Hi, I need help with my order #12345', messageType: 'TEXT', senderType: 'CUSTOMER', senderId: 'cust-001', senderName: 'Alice Johnson', sequence: 1, createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString() },
      { id: 'msg-002', messageId: 'msg-002', conversationId: 'conv-001', content: 'Hello! I can help you with that. Let me check the order status.', messageType: 'TEXT', senderType: 'AGENT', senderId: 'agent-001', senderName: 'John Doe', sequence: 2, createdAt: new Date(Date.now() - 1000 * 60 * 28).toISOString() },
      { id: 'msg-003', messageId: 'msg-003', conversationId: 'conv-001', content: 'Your order is currently being processed and will ship today.', messageType: 'TEXT', senderType: 'AGENT', senderId: 'agent-001', senderName: 'John Doe', sequence: 3, createdAt: new Date(Date.now() - 1000 * 60 * 27).toISOString() },
      { id: 'msg-004', messageId: 'msg-004', conversationId: 'conv-001', content: 'Great! When will it arrive?', messageType: 'TEXT', senderType: 'CUSTOMER', senderId: 'cust-001', senderName: 'Alice Johnson', sequence: 4, createdAt: new Date(Date.now() - 1000 * 60 * 2).toISOString() }
    ],
    'conv-002': [
      { id: 'msg-005', messageId: 'msg-005', conversationId: 'conv-002', content: 'I was charged twice for my order', messageType: 'TEXT', senderType: 'CUSTOMER', senderId: 'cust-002', senderName: 'Bob Smith', sequence: 1, createdAt: new Date(Date.now() - 1000 * 60 * 60).toISOString() },
      { id: 'msg-006', messageId: 'msg-006', conversationId: 'conv-002', content: 'I apologize for the inconvenience. Let me investigate this for you.', messageType: 'TEXT', senderType: 'AGENT', senderId: 'agent-001', senderName: 'John Doe', sequence: 2, createdAt: new Date(Date.now() - 1000 * 60 * 55).toISOString() },
      { id: 'msg-007', messageId: 'msg-007', conversationId: 'conv-002', content: 'I can see there was a duplicate charge. I\'ve initiated a refund for one of them.', messageType: 'TEXT', senderType: 'AGENT', senderId: 'agent-001', senderName: 'John Doe', sequence: 3, createdAt: new Date(Date.now() - 1000 * 60 * 50).toISOString() }
    ],
    'conv-003': [
      { id: 'msg-008', messageId: 'msg-008', conversationId: 'conv-003', content: 'Where is my order? It\'s been 2 weeks!', messageType: 'TEXT', senderType: 'CUSTOMER', senderId: 'cust-003', senderName: 'Charlie Brown', sequence: 1, createdAt: new Date(Date.now() - 1000 * 60 * 10).toISOString() }
    ]
  };

  getMockConversations(status?: string, agentId?: string): Observable<any> {
    let filtered = [...this.mockConversations];
    
    if (status) {
      filtered = filtered.filter(c => c.status === status);
    }
    
    if (agentId) {
      filtered = filtered.filter(c => c.assignedAgentId === agentId);
    }
    
    return of(filtered).pipe(delay(300)); // Simulate network delay
  }

  getMockMessages(conversationId: string): Observable<any> {
    const messages = this.mockMessages[conversationId] || [];
    return of(messages).pipe(delay(200));
  }

  getMockConversation(conversationId: string): Observable<any> {
    const conversation = this.mockConversations.find(c => c.conversationId === conversationId);
    return of(conversation).pipe(delay(200));
  }

  addMockMessage(conversationId: string, content: string, agentId: string, agentName: string): Observable<any> {
    const newMessage = {
      messageId: 'msg-' + Math.random().toString(36).substr(2, 9),
      conversationId,
      content,
      senderType: 'AGENT',
      senderId: agentId,
      senderName: agentName,
      sentAt: new Date().toISOString()
    };

    if (!this.mockMessages[conversationId]) {
      this.mockMessages[conversationId] = [];
    }

    this.mockMessages[conversationId].push(newMessage);

    // Update conversation lastMessageAt
    const conversation = this.mockConversations.find(c => c.conversationId === conversationId);
    if (conversation) {
      conversation.lastMessageAt = newMessage.sentAt;
    }

    return of(newMessage).pipe(delay(300));
  }

  createMockAgent(name: string, email: string, skills: string): Observable<any> {
    const newAgent = {
      agentId: 'agent-001', // Use fixed ID for easy testing with mock conversations
      agentName: name,
      email,
      status: 'ONLINE',
      skills,
      currentChatCount: 0,
      maxConcurrentChats: 5
    };

    // Don't add to array to avoid duplicates
    // this.mockAgents.push(newAgent);

    return of(newAgent).pipe(delay(500));
  }

  getMockAgent(agentId: string): Observable<any> {
    const agent = this.mockAgents.find(a => a.agentId === agentId);
    return of(agent).pipe(delay(200));
  }
}
