import { Component, OnInit, OnDestroy } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { ChatMessage, MessageType, SenderType, ConversationResponse } from '../../core/models/chat.model';
import { Subscription } from 'rxjs';

const LS_USER_ID = 'chat_userId';
const LS_CONV_ID = 'chat_conversationId';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './chat-widget.component.html',
  styleUrl: './chat-widget.component.scss'
})
export class ChatWidgetComponent implements OnInit, OnDestroy {
  isOpen = false;
  isWebViewMode = false;

  userId: string = '';
  conversationId: string | null = null;

  messages: ChatMessage[] = [];
  inputMessage = '';
  loading = false;
  agentTyping = false;

  readonly SenderType = SenderType;

  private subs = new Subscription();

  constructor(
    private apiService: ApiService,
    private wsService: WebSocketService
  ) {}

  ngOnInit(): void {
    // --- Restore hoặc tạo userId mới, lưu vào localStorage ---
    let savedUserId = localStorage.getItem(LS_USER_ID);
    if (!savedUserId) {
      savedUserId = 'cust-' + Math.floor(1000 + Math.random() * 9000);
      localStorage.setItem(LS_USER_ID, savedUserId);
    }
    this.userId = savedUserId;

    // --- Restore conversationId nếu đã có ---
    const savedConvId = localStorage.getItem(LS_CONV_ID);
    if (savedConvId) {
      this.conversationId = savedConvId;
    }

    // --- Webview mode ---
    const params = new URLSearchParams(window.location.search);
    if (params.get('mode') === 'webview') {
      this.isWebViewMode = true;
      this.isOpen = true;
    }

    // --- Tự động mở và khôi phục nếu đã có conversation ---
    if (this.conversationId) {
      this.isOpen = true;
      this.restoreSession();
    }
  }

  toggleWidget(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen && !this.conversationId) {
      this.startConversation();
    }
  }

  /** Khôi phục session sau reload: load lịch sử + reconnect WebSocket */
  private restoreSession(): void {
    this.loading = true;
    this.listenMessages();
    this.loadHistory();

    this.wsService.connect(this.userId);
    const connSub = this.wsService.isConnected().subscribe(connected => {
      if (connected && this.conversationId) {
        this.wsService.subscribeToConversation(this.conversationId);
        connSub.unsubscribe();
      }
    });
    this.subs.add(connSub);
  }

  startConversation(): void {
    this.loading = true;
    this.apiService.createConversation({
      userId: this.userId,
      topicCode: 'ORDER_SUPPORT'
    }).subscribe({
      next: (res: ConversationResponse) => {
        this.conversationId = res.conversationId;
        // Persist để reload vẫn giữ được session
        localStorage.setItem(LS_CONV_ID, res.conversationId);
        this.loading = false;

        this.listenMessages();
        this.loadHistory();

        this.wsService.connect(this.userId);
        const connSub = this.wsService.isConnected().subscribe(connected => {
          if (connected && this.conversationId) {
            this.wsService.subscribeToConversation(this.conversationId);
            connSub.unsubscribe();
          }
        });
        this.subs.add(connSub);
      },
      error: (err: any) => {
        console.error('Failed to create conversation', err);
        this.loading = false;
      }
    });
  }

  loadHistory(): void {
    if (!this.conversationId) return;
    this.apiService.getMessages(this.conversationId).subscribe({
      next: (res: ChatMessage[]) => {
        this.messages = res || [];
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  listenMessages(): void {
    this.subs.add(
      this.wsService.getMessages().subscribe((msg: ChatMessage) => {
        if (msg.conversationId === this.conversationId) {
          if (!this.messages.some(m => m.id === msg.id)) {
            this.messages.push(msg);
          }
        }
      })
    );

    this.subs.add(
      this.wsService.getTypingIndicators().subscribe((ind: { conversationId: string; isTyping: boolean }) => {
        if (ind.conversationId === this.conversationId) {
          this.agentTyping = ind.isTyping;
        }
      })
    );
  }

  sendMessage(): void {
    if (!this.inputMessage.trim() || !this.conversationId) return;

    const text = this.inputMessage.trim();
    this.inputMessage = '';

    const req = {
      conversationId: this.conversationId,
      content: text,
      messageType: MessageType.TEXT,
      senderId: this.userId,
      senderType: SenderType.USER,
      clientMessageId: 'msg-' + Date.now()
    };

    this.apiService.sendMessage(req).subscribe({
      error: (err: any) => {
        console.error('Failed to send message', err);
      }
    });
  }

  /** Xóa session — dùng khi conversation CLOSED hoặc muốn bắt đầu chat mới */
  clearSession(): void {
    localStorage.removeItem(LS_CONV_ID);
    this.conversationId = null;
    this.messages = [];
    this.isOpen = false;
    this.wsService.disconnect();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.wsService.disconnect();
  }
}
