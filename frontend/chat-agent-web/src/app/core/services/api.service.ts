import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, catchError } from 'rxjs';
import { map } from 'rxjs/operators';
import { Conversation, ConversationListResponse } from '../models/conversation.model';
import { ChatMessage, MessageListResponse, SendMessageRequest } from '../models/message.model';
import { MockDataService } from './mock-data.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  // chat-core-service chạy port 8080 (xem application.yml)
  private readonly BASE = environment.apiUrl;
  private readonly useMock = environment.useMockData;
  private mockService = inject(MockDataService);

  constructor(private http: HttpClient) {}

  // ==================== Auth / Bootstrap ====================

  /**
   * Bootstrap: tạo conversation cho customer
   * POST /api/v1/chat/bootstrap
   */
  bootstrap(userId: string, merchantId: string = 'merchant-001', topicCode: string = 'ORDER_SUPPORT'): Observable<any> {
    if (this.useMock) {
      return of({ conversationId: 'conv-' + Math.random().toString(36).substr(2, 9), userId, merchantId, topicCode });
    }
    return this.http.post<any>(`${this.BASE}/chat/bootstrap`, {
      userId,
      merchantId,
      topicCode
    });
  }

  // ==================== Conversations ====================

  /**
   * Lấy danh sách conversation của agent (các conv đã assigned cho agentId)
   * GET /api/v1/conversations?userId={agentId}
   */
  getAgentConversations(agentId: string): Observable<ConversationListResponse> {
    if (this.useMock) {
      return this.mockService.getMockConversations(undefined, agentId).pipe(
        map(list => ({
          content: list,
          totalElements: list.length,
          totalPages: 1,
          page: 0,
          size: list.length
        }))
      );
    }
    
    return this.http.get<any[]>(`${this.BASE}/conversations`, {
      params: new HttpParams().set('agentId', agentId)
    }).pipe(
      map(list => ({
        content: list.map(c => this.mapConversation(c)),
        totalElements: list.length,
        totalPages: 1,
        page: 0,
        size: list.length
      }))
    );
  }

  /**
   * Lấy danh sách conversation đang chờ (queue)
   * GET /api/v1/conversations/queue
   */
  getPendingConversations(): Observable<ConversationListResponse> {
    if (this.useMock) {
      return this.mockService.getMockConversations('WAITING').pipe(
        map(list => ({
          content: list,
          totalElements: list.length,
          totalPages: 1,
          page: 0,
          size: list.length
        }))
      );
    }
    
    return this.http.get<any[]>(`${this.BASE}/conversations/queue`).pipe(
      map(list => ({
        content: list.map(c => this.mapConversation(c)),
        totalElements: list.length,
        totalPages: 1,
        page: 0,
        size: list.length
      }))
    );
  }

  /**
   * Lấy chi tiết conversation
   * GET /api/v1/conversations/{id}
   */
  getConversationById(conversationId: string): Observable<Conversation> {
    if (this.useMock) {
      return this.mockService.getMockConversation(conversationId);
    }
    
    return this.http.get<any>(`${this.BASE}/conversations/${conversationId}`)
      .pipe(map(c => this.mapConversation(c)));
  }

  /**
   * Agent nhận conversation
   * POST /api/v1/conversations/{id}/assign
   */
  assignConversation(conversationId: string, agentId: string): Observable<Conversation> {
    if (this.useMock) {
      return this.mockService.getMockConversation(conversationId).pipe(
        map(conv => ({ ...conv, assignedAgentId: agentId, status: 'ACTIVE' }))
      );
    }
    
    return this.http.post<any>(
      `${this.BASE}/conversations/${conversationId}/assign`,
      { agentId }
    ).pipe(map(c => this.mapConversation(c)));
  }

  /**
   * Đóng conversation
   * POST /api/v1/conversations/{id}/close
   */
  closeConversation(conversationId: string): Observable<Conversation> {
    if (this.useMock) {
      return this.mockService.getMockConversation(conversationId).pipe(
        map(conv => ({ ...conv, status: 'CLOSED' }))
      );
    }
    
    return this.http.post<any>(
      `${this.BASE}/conversations/${conversationId}/close`,
      {}
    ).pipe(map(c => this.mapConversation(c)));
  }

  /**
   * Transfer conversation sang agent khác
   * POST /api/v1/conversations/{id}/transfer
   */
  transferConversation(conversationId: string, toAgentId: string, transferredBy: string): Observable<Conversation> {
    if (this.useMock) {
      return this.mockService.getMockConversation(conversationId).pipe(
        map(conv => ({ ...conv, assignedAgentId: toAgentId }))
      );
    }
    
    return this.http.post<any>(
      `${this.BASE}/conversations/${conversationId}/transfer`,
      { toAgentId, transferredBy }
    ).pipe(map(c => this.mapConversation(c)));
  }

  // ==================== Messages ====================

  /**
   * Lấy lịch sử messages
   * GET /api/v1/conversations/{conversationId}/messages
   */
  getConversationMessages(conversationId: string): Observable<MessageListResponse> {
    if (this.useMock) {
      return this.mockService.getMockMessages(conversationId).pipe(
        map(list => ({
          content: list,
          totalElements: list.length,
          page: 0,
          size: list.length
        }))
      );
    }
    
    return this.http.get<any[]>(
      `${this.BASE}/conversations/${conversationId}/messages`
    ).pipe(
      map(list => ({
        content: list.map(m => this.mapMessage(m)),
        totalElements: list.length,
        page: 0,
        size: list.length
      }))
    );
  }

  /**
   * Lấy messages mới sau seqNo (polling / sync)
   * GET /api/v1/conversations/{conversationId}/messages?afterSequence={n}
   */
  getNewMessages(conversationId: string, afterSequence: number): Observable<ChatMessage[]> {
    if (this.useMock) {
      return of([]);
    }
    
    return this.http.get<any[]>(
      `${this.BASE}/conversations/${conversationId}/messages`,
      { params: new HttpParams().set('afterSequence', afterSequence.toString()) }
    ).pipe(map(list => list.map(m => this.mapMessage(m))));
  }

  /**
   * Gửi tin nhắn
   * POST /api/v1/conversations/{conversationId}/messages
   */
  sendMessage(request: SendMessageRequest): Observable<ChatMessage> {
    if (this.useMock) {
      return this.mockService.addMockMessage(
        request.conversationId,
        request.content,
        request.senderId,
        request.senderId // senderName fallback
      );
    }
    
    return this.http.post<any>(
      `${this.BASE}/conversations/${request.conversationId}/messages`,
      {
        senderId:        request.senderId,
        senderType:      request.senderType,
        messageType:     request.messageType,
        content:         request.content,
        clientMessageId: request.clientMessageId
      }
    ).pipe(map(m => this.mapMessage(m)));
  }

  // ==================== Agents ====================

  /**
   * Tạo agent mới
   * POST /api/v1/agents
   */
  createAgent(name: string, email: string, skills: string = '["ORDER_SUPPORT"]'): Observable<any> {
    if (this.useMock) {
      return this.mockService.createMockAgent(name, email, skills);
    }
    
    return this.http.post<any>(`${this.BASE}/agents`, {
      name, email, skills, maxConcurrentChats: 5, createdBy: 'admin'
    });
  }

  /**
   * Lấy agent theo ID
   */
  getAgentById(agentId: string): Observable<any> {
    if (this.useMock) {
      return this.mockService.getMockAgent(agentId);
    }
    
    return this.http.get<any>(`${this.BASE}/agents/${agentId}`);
  }

  /**
   * Lấy danh sách agents online
   */
  getAllAgents(): Observable<any[]> {
    if (this.useMock) {
      return of([
        { agentId: 'agent-001', agentName: 'John Doe', status: 'ONLINE', currentChatCount: 2 },
        { agentId: 'agent-002', agentName: 'Jane Smith', status: 'ONLINE', currentChatCount: 1 },
      ]);
    }
    
    return this.http.get<any>(`${this.BASE}/agents/online`).pipe(
      map(res => res.agents ?? [])
    );
  }

  /**
   * Cập nhật trạng thái agent
   * PUT /api/v1/agents/{agentId}/status
   */
  updateAgentStatus(agentId: string, status: 'ONLINE' | 'OFFLINE' | 'BUSY' | 'AWAY'): Observable<any> {
    if (this.useMock) {
      return of({ agentId, status });
    }
    
    return this.http.put<any>(`${this.BASE}/agents/${agentId}/status`, { status });
  }

  /**
   * Heartbeat agent (keep alive)
   */
  agentHeartbeat(agentId: string): Observable<any> {
    if (this.useMock) {
      return of({ agentId, timestamp: new Date().toISOString() });
    }
    
    return this.http.post<any>(`${this.BASE}/agents/${agentId}/heartbeat`, {});
  }

  // ==================== Canned Replies ====================

  /**
   * GET /api/v1/canned-replies
   */
  getCannedReplies(): Observable<any[]> {
    if (this.useMock) {
      return of([
        { id: '1', shortcut: '/thanks', message: 'Thank you for contacting us!' },
        { id: '2', shortcut: '/check', message: 'Let me check that for you.' },
        { id: '3', shortcut: '/refund', message: 'I will process your refund right away.' }
      ]);
    }
    
    return this.http.get<any[]>(`${this.BASE}/canned-replies`);
  }

  // ==================== Supervisor ====================

  /**
   * Dashboard tổng quan
   * GET /api/v1/supervisor/dashboard
   */
  getDashboardStats(): Observable<any> {
    if (this.useMock) {
      return of({
        totalActive: 5,
        totalPending: 3,
        totalResolved: 42,
        avgWaitingTime: 120,
        avgHandlingTime: 480,
        agentsOnline: 3,
        agentsBusy: 2,
        agentsOffline: 1,
        slaBreached: 1,
        slaMet: 41
      });
    }
    
    return this.http.get<any>(`${this.BASE}/supervisor/dashboard`).pipe(
      map(data => this.mapDashboard(data))
    );
  }

  /**
   * Queue detail từ supervisor
   * GET /api/v1/supervisor/queue
   */
  getQueueStats(): Observable<any> {
    if (this.useMock) {
      return of({ queueDepth: 3, avgWaitTime: 120, longestWait: 300 });
    }
    
    return this.http.get<any>(`${this.BASE}/supervisor/queue`);
  }

  /**
   * SLA violations
   */
  getSlaViolations(): Observable<any> {
    if (this.useMock) {
      return of([
        { conversationId: 'conv-005', customerName: 'David Lee', waitTime: 600, slaThreshold: 300 }
      ]);
    }
    
    return this.http.get<any>(`${this.BASE}/supervisor/sla/violations`);
  }

  /**
   * Performance metrics
   */
  getPerformanceMetrics(startDate: string, endDate: string): Observable<any> {
    if (this.useMock) {
      return of({
        totalHandled: 42,
        avgHandlingTime: 480,
        csatScore: 4.5,
        firstResponseTime: 60
      });
    }
    
    return this.http.get<any>(`${this.BASE}/supervisor/performance`, {
      params: new HttpParams().set('startDate', startDate).set('endDate', endDate)
    });
  }

  // ==================== Ratings ====================

  /**
   * Submit CSAT rating
   * POST /api/v1/ratings/conversations/{conversationId}
   */
  submitRating(conversationId: string, rating: number, comment: string = ''): Observable<any> {
    if (this.useMock) {
      return of({ conversationId, rating, comment, submittedAt: new Date().toISOString() });
    }
    
    return this.http.post<any>(
      `${this.BASE}/ratings/conversations/${conversationId}`,
      { rating, comment }
    );
  }

  getCsatScore(): Observable<any> {
    if (this.useMock) {
      return of({ score: 4.5, totalRatings: 42 });
    }
    
    return this.http.get<any>(`${this.BASE}/ratings/csat/score`);
  }

  // ==================== Presence (realtime-service) ====================

  /** Realtime service chạy port 8083 */
  private readonly REALTIME_BASE = 'http://localhost:8083/api';

  getUserPresence(userId: string): Observable<any> {
    if (this.useMock) {
      return of({ userId, status: 'ONLINE', lastSeen: new Date().toISOString() });
    }
    
    return this.http.get<any>(`${this.REALTIME_BASE}/presence/${userId}`);
  }

  getBulkPresence(userIds: string[]): Observable<any> {
    if (this.useMock) {
      return of(userIds.map(id => ({ userId: id, status: 'ONLINE' })));
    }
    
    return this.http.post<any>(`${this.REALTIME_BASE}/presence/bulk`, { userIds });
  }

  updatePresence(userId: string, status: 'ONLINE' | 'AWAY' | 'OFFLINE'): Observable<any> {
    if (this.useMock) {
      return of({ userId, status });
    }
    
    return this.http.put<any>(`${this.REALTIME_BASE}/presence/${userId}/status`, { status });
  }

  // ==================== Mappers ====================

  private mapConversation(c: any): Conversation {
    return {
      id:             c.conversationId ?? c.id,
      customerId:     c.userId ?? '',
      customerName:   c.userId ?? 'Unknown',    // backend không có tên KH, dùng userId
      agentId:        c.assignedAgent ?? undefined,
      agentName:      c.assignedAgent ?? undefined,
      status:         this.mapStatus(c.status),
      priority:       'NORMAL',
      topicCode:      c.topicCode ?? undefined,
      channel:        'WEB',
      createdAt:      c.createdAt ?? '',
      updatedAt:      c.updatedAt ?? c.createdAt ?? '',
      lastMessageAt:  c.lastMessageAt ?? c.updatedAt ?? c.createdAt
    } as any;
  }

  private mapMessage(m: any): ChatMessage {
    return {
      id:             m.messageId ?? m.id,
      conversationId: m.conversationId,
      content:        m.content ?? '',
      messageType:    m.messageType ?? 'TEXT',
      senderType:     m.senderType ?? 'USER',
      senderId:       m.senderId ?? '',
      senderName:     m.senderId ?? '',
      sequence:       m.seqNo ?? m.sequence ?? 0,
      createdAt:      m.createdAt ?? '',
    } as any;
  }

  private mapStatus(status: string): any {
    const map: Record<string, string> = {
      'NEW':         'PENDING',
      'IN_PROGRESS': 'ACTIVE',
      'CLOSED':      'CLOSED',
      'RESOLVED':    'RESOLVED'
    };
    return map[status] ?? status;
  }

  private mapDashboard(data: any): any {
    const agent = data.agentMetrics ?? {};
    const queue = data.queueMetrics ?? {};
    return {
      totalActive:       agent.busyCount       ?? 0,
      totalPending:      queue.queueDepth       ?? 0,
      totalResolved:     0,
      avgWaitingTime:    queue.avgWaitTime      ?? 0,
      avgHandlingTime:   0,
      agentsOnline:      agent.onlineCount      ?? 0,
      agentsBusy:        agent.busyCount        ?? 0,
      agentsOffline:     0,
      slaBreached:       data.slaMetrics?.violationsCount ?? 0,
      slaMet:            0
    };
  }
}
