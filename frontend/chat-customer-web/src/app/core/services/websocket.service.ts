import { Injectable } from '@angular/core';
import { Client, StompConfig, IMessage } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client';
import { ChatMessage } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;
  private connected$ = new BehaviorSubject<boolean>(false);
  private messages$ = new Subject<ChatMessage>();
  private typingIndicators$ = new Subject<{ conversationId: string; isTyping: boolean }>();
  private conversationClosed$ = new Subject<string>();

  private readonly WS_URL = 'http://localhost:8083/ws'; // chat-realtime-service

  connect(userId: string): void {
    if (this.client && this.client.connected) return;

    const stompConfig: StompConfig = {
      webSocketFactory: () => new SockJS(this.WS_URL) as any,
      connectHeaders: {
        userId: userId,
        userType: 'CUSTOMER'
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('Customer WebSocket connected');
        this.connected$.next(true);
      },
      onDisconnect: () => {
        console.log('Customer WebSocket disconnected');
        this.connected$.next(false);
      }
    };

    this.client = new Client(stompConfig);
    this.client.activate();
  }

  subscribeToConversation(conversationId: string): void {
    if (!this.client || !this.client.connected) {
      console.warn('Customer WS: subscribeToConversation called but not connected');
      return;
    }

    console.log('Customer WS: Subscribing to conversation:', conversationId);
    this.client.subscribe(`/topic/conversation/${conversationId}`, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body);
        console.log('Customer WS: Received WS message:', data);
        if (data.type === 'TYPING_START' || data.type === 'TYPING_STOP') {
          this.typingIndicators$.next({
            conversationId: data.conversationId,
            isTyping: data.type === 'TYPING_START'
          });
        } else if (data.type === 'CONVERSATION_CLOSED') {
          this.conversationClosed$.next(data.conversationId);
        } else if (data.type === 'MESSAGE' || data.type === 'MESSAGE_CREATED') {
          const chatMsg: ChatMessage = data.payload || data;
          console.log('Customer WS: New chat message:', chatMsg);
          this.messages$.next(chatMsg);
        }
      } catch (e) {
        console.error('Error parsing WebSocket message', e);
      }
    });
  }

  sendTypingIndicator(conversationId: string, isTyping: boolean): void {
    if (!this.client || !this.client.connected) return;
    this.client.publish({
      destination: isTyping ? '/app/typing.start' : '/app/typing.stop',
      body: JSON.stringify({ conversationId })
    });
  }

  getMessages(): Observable<ChatMessage> {
    return this.messages$.asObservable();
  }

  getTypingIndicators(): Observable<{ conversationId: string; isTyping: boolean }> {
    return this.typingIndicators$.asObservable();
  }

  getConversationClosed(): Observable<string> {
    return this.conversationClosed$.asObservable();
  }

  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connected$.next(false);
    }
  }
}
