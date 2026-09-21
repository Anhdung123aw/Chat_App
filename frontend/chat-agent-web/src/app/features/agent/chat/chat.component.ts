import {
  Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { ChatMessage, MessageType, SenderType, SendMessageRequest } from '../../../core/models/message.model';
import { Conversation } from '../../../core/models/conversation.model';
import { WebSocketMessage, WebSocketMessageType } from '../../../core/models/websocket.model';
import { CannedRepliesComponent } from '../canned-replies/canned-replies.component';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, CannedRepliesComponent],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messageContainer') messageContainer!: ElementRef;

  conversation: Conversation | null = null;
  messages: ChatMessage[] = [];
  newMessage = '';
  loading = false;
  sending = false;
  isTyping = false;         // customer is typing
  showCannedReplies = false;

  conversationId!: string;
  private destroy$ = new Subject<void>();
  private typingTimeout: any;
  private shouldScrollToBottom = false;

  readonly MessageType = MessageType;
  readonly SenderType = SenderType;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
    private authService: AuthService,
    private wsService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params['conversationId'];
      if (id) {
        if (this.conversationId) {
          this.wsService.unsubscribeFromConversation(this.conversationId);
        }
        this.conversationId = id;
        this.loadConversation();
        this.loadMessages();
        this.subscribeToConversation();
      }
    });

    this.wsService.getTypingIndicators().pipe(takeUntil(this.destroy$)).subscribe(indicator => {
      if (indicator.conversationId === this.conversationId) {
        this.isTyping = indicator.isTyping;
        if (this.isTyping) this.scrollToBottom();
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    if (this.conversationId) {
      this.wsService.unsubscribeFromConversation(this.conversationId);
    }
    clearTimeout(this.typingTimeout);
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadConversation(): void {
    this.apiService.getConversationById(this.conversationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe(conv => { this.conversation = conv; });
  }

  loadMessages(): void {
    this.loading = true;
    this.apiService.getConversationMessages(this.conversationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.messages = res.content.sort((a, b) => a.sequence - b.sequence);
          this.loading = false;
          this.shouldScrollToBottom = true;
        },
        error: () => { this.loading = false; }
      });
  }

  subscribeToConversation(): void {
    this.wsService.isConnected().pipe(takeUntil(this.destroy$)).subscribe(connected => {
      if (connected && this.conversationId) {
        this.wsService.subscribeToConversation(this.conversationId)
          .pipe(takeUntil(this.destroy$))
          .subscribe((msg: WebSocketMessage) => {
        if (msg.type === WebSocketMessageType.MESSAGE && msg.payload) {
          const chatMsg: ChatMessage = msg.payload as ChatMessage;
          // Avoid duplicate
          if (!this.messages.find(m => m.id === chatMsg.id)) {
            this.messages.push(chatMsg);
            this.shouldScrollToBottom = true;
          }
        }
        if (
          msg.type === WebSocketMessageType.CONVERSATION_CLOSED ||
          msg.type === WebSocketMessageType.CONVERSATION_STATUS_CHANGED
        ) {
          this.loadConversation();
        }
      });
      }
    });
  }

  sendMessage(): void {
    const content = this.newMessage.trim();
    if (!content || this.sending) return;

    this.sending = true;
    const user = this.authService.getCurrentUser();

    // Optimistic add
    const tempMsg: ChatMessage = {
      id: `temp-${Date.now()}`,
      conversationId: this.conversationId,
      content,
      messageType: MessageType.TEXT,
      senderType: SenderType.AGENT,
      senderId: user?.id ?? '',
      senderName: user?.fullName,
      sequence: this.messages.length + 1,
      createdAt: new Date().toISOString()
    };
    this.messages.push(tempMsg);
    this.newMessage = '';
    this.shouldScrollToBottom = true;

    const request: SendMessageRequest = {
      conversationId: this.conversationId,
      content,
      messageType: MessageType.TEXT,
      senderId:        user?.id ?? '',
      senderType:      SenderType.AGENT,
      clientMessageId: `${user?.id}-${Date.now()}`
    };

    this.apiService.sendMessage(request).pipe(takeUntil(this.destroy$)).subscribe({
      next: (saved) => {
        const idx = this.messages.findIndex(m => m.id === tempMsg.id);
        if (idx !== -1) this.messages[idx] = saved;
        this.sending = false;
        // Stop typing
        this.wsService.sendTypingIndicator(this.conversationId, false);
      },
      error: () => {
        // Remove temp on error
        this.messages = this.messages.filter(m => m.id !== tempMsg.id);
        this.newMessage = content;
        this.sending = false;
      }
    });
  }

  onInputChange(): void {
    this.wsService.sendTypingIndicator(this.conversationId, true);
    clearTimeout(this.typingTimeout);
    this.typingTimeout = setTimeout(() => {
      this.wsService.sendTypingIndicator(this.conversationId, false);
    }, 2000);
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  closeConversation(): void {
    if (!confirm('Close this conversation?')) return;
    this.apiService.closeConversation(this.conversationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.loadConversation();
      });
  }

  selectCannedReply(text: string): void {
    this.newMessage = text;
    this.showCannedReplies = false;
  }

  toggleCannedReplies(): void {
    this.showCannedReplies = !this.showCannedReplies;
  }

  isMine(msg: ChatMessage): boolean {
    const user = this.authService.getCurrentUser();
    return msg.senderType === SenderType.AGENT && msg.senderId === user?.id;
  }

  isSystem(msg: ChatMessage): boolean {
    return msg.senderType === SenderType.SYSTEM;
  }

  scrollToBottom(): void {
    try {
      const el = this.messageContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch { /* ignore */ }
  }

  formatTime(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  }

  goBack(): void {
    this.router.navigate(['/agent/conversations']);
  }
}
