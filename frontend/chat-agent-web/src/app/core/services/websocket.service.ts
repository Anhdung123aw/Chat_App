import { Injectable } from '@angular/core';
import { Client, StompConfig, IMessage } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { WebSocketMessage, WebSocketMessageType, TypingIndicator } from '../models/websocket.model';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;
  private connected$ = new BehaviorSubject<boolean>(false);
  private messages$ = new Subject<WebSocketMessage>();
  private typingIndicators$ = new Subject<TypingIndicator>();
  
  private readonly WS_URL = 'http://localhost:8083/ws'; // chat-realtime-service
  private userId: string | null = null;
  private userType: string = 'AGENT';

  constructor() {}

  /**
   * Connect to WebSocket server
   */
  connect(userId: string, userType: 'AGENT' | 'SUPERVISOR' = 'AGENT'): void {
    if (this.client && this.client.connected) {
      console.log('WebSocket already connected');
      return;
    }

    this.userId = userId;
    this.userType = userType;

    const stompConfig: StompConfig = {
      webSocketFactory: () => new SockJS(this.WS_URL) as any,
      
      connectHeaders: {
        userId: userId,
        userType: userType
      },

      debug: (msg: string) => {
        console.log('STOMP Debug:', msg);
      },

      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('WebSocket connected');
        this.connected$.next(true);
        this.setupSubscriptions();
      },

      onDisconnect: () => {
        console.log('WebSocket disconnected');
        this.connected$.next(false);
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      }
    };

    this.client = new Client(stompConfig);
    this.client.activate();
  }

  /**
   * Setup message subscriptions
   */
  private setupSubscriptions(): void {
    if (!this.client || !this.userId) return;

    // Subscribe to user-specific queue
    this.client.subscribe(`/user/queue/messages`, (message: IMessage) => {
      this.handleIncomingMessage(message);
    });

    // Subscribe to global broadcast
    this.client.subscribe(`/topic/broadcast`, (message: IMessage) => {
      this.handleIncomingMessage(message);
    });

    console.log('WebSocket subscriptions setup complete');
  }

  /**
   * Handle incoming WebSocket message
   */
  private handleIncomingMessage(message: IMessage): void {
    try {
      const wsMessage: WebSocketMessage = JSON.parse(message.body);
      console.log('Received WebSocket message:', wsMessage);

      // Handle different message types
      switch (wsMessage.type) {
        case WebSocketMessageType.TYPING_START:
        case WebSocketMessageType.TYPING_STOP:
          this.handleTypingEvent(wsMessage);
          break;
        
        default:
          this.messages$.next(wsMessage);
      }
    } catch (error) {
      console.error('Error parsing WebSocket message:', error);
    }
  }

  /**
   * Handle typing indicator events
   */
  private handleTypingEvent(message: WebSocketMessage): void {
    if (!message.conversationId || !message.senderId) return;

    const indicator: TypingIndicator = {
      conversationId: message.conversationId,
      userId: message.senderId,
      isTyping: message.type === WebSocketMessageType.TYPING_START
    };

    this.typingIndicators$.next(indicator);
  }

  /**
   * Subscribe to a conversation
   */
  subscribeToConversation(conversationId: string): Observable<WebSocketMessage> {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected');
      return new Subject<WebSocketMessage>().asObservable();
    }

    const subject = new Subject<WebSocketMessage>();

    // Subscribe to conversation topic
    this.client.subscribe(`/topic/conversation/${conversationId}`, (message: IMessage) => {
      try {
        const wsMessage: WebSocketMessage = JSON.parse(message.body);
        subject.next(wsMessage);
      } catch (error) {
        console.error('Error parsing conversation message:', error);
      }
    });

    // Send subscribe request to backend
    this.client.publish({
      destination: '/app/conversation.subscribe',
      body: JSON.stringify({ conversationId })
    });

    console.log(`Subscribed to conversation: ${conversationId}`);
    return subject.asObservable();
  }

  /**
   * Unsubscribe from a conversation
   */
  unsubscribeFromConversation(conversationId: string): void {
    if (!this.client || !this.client.connected) return;

    this.client.publish({
      destination: '/app/conversation.unsubscribe',
      body: JSON.stringify({ conversationId })
    });

    console.log(`Unsubscribed from conversation: ${conversationId}`);
  }

  /**
   * Send typing indicator
   */
  sendTypingIndicator(conversationId: string, isTyping: boolean): void {
    if (!this.client || !this.client.connected) return;

    const destination = isTyping ? '/app/typing.start' : '/app/typing.stop';
    
    this.client.publish({
      destination,
      body: JSON.stringify({ conversationId })
    });
  }

  /**
   * Send heartbeat ping
   */
  sendPing(): void {
    if (!this.client || !this.client.connected) return;

    this.client.publish({
      destination: '/app/ping',
      body: '{}'
    });
  }

  /**
   * Disconnect WebSocket
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connected$.next(false);
    }
  }

  /**
   * Observable for connection status
   */
  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  /**
   * Observable for incoming messages
   */
  getMessages(): Observable<WebSocketMessage> {
    return this.messages$.asObservable();
  }

  /**
   * Observable for typing indicators
   */
  getTypingIndicators(): Observable<TypingIndicator> {
    return this.typingIndicators$.asObservable();
  }
}
