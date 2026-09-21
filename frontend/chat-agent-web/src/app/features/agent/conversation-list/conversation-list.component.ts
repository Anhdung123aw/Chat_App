import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, interval } from 'rxjs';
import { takeUntil, switchMap } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Conversation, ConversationStatus } from '../../../core/models/conversation.model';
import { WebSocketMessageType } from '../../../core/models/websocket.model';

@Component({
  selector: 'app-conversation-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './conversation-list.component.html',
  styleUrl: './conversation-list.component.scss'
})
export class ConversationListComponent implements OnInit, OnDestroy {
  conversations: Conversation[] = [];
  filteredConversations: Conversation[] = [];
  selectedFilter: 'ALL' | 'ACTIVE' | 'PENDING' | 'WAITING' = 'ALL';
  searchQuery = '';
  loading = false;
  selectedConversationId: string | null = null;

  private destroy$ = new Subject<void>();
  readonly ConversationStatus = ConversationStatus;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private wsService: WebSocketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadConversations();
    this.subscribeToWebSocketEvents();
    // Refresh mỗi 30 giây
    interval(30000).pipe(takeUntil(this.destroy$)).subscribe(() => this.loadConversations());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadConversations(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.loading = true;
    import('rxjs').then(({ forkJoin }) => {
      forkJoin({
        agentConvs: this.apiService.getAgentConversations(user.id),
        queueConvs: this.apiService.getPendingConversations()
      }).pipe(takeUntil(this.destroy$)).subscribe({
        next: (res) => {
          const all = [...res.agentConvs.content, ...res.queueConvs.content];
          // Lọc trùng lặp nếu có
          this.conversations = Array.from(new Map(all.map(c => [c.id, c])).values());
          this.applyFilter();
          this.loading = false;
        },
        error: () => this.loading = false
      });
    });
  }

  subscribeToWebSocketEvents(): void {
    this.wsService.getMessages().pipe(takeUntil(this.destroy$)).subscribe(msg => {
      if (
        msg.type === WebSocketMessageType.CONVERSATION_ASSIGNED ||
        msg.type === WebSocketMessageType.CONVERSATION_STATUS_CHANGED
      ) {
        this.loadConversations();
      }
    });
  }

  applyFilter(): void {
    let list = [...this.conversations];
    if (this.selectedFilter !== 'ALL') {
      list = list.filter(c => c.status === this.selectedFilter);
    }
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      list = list.filter(c =>
        c.customerName.toLowerCase().includes(q) ||
        c.id.toLowerCase().includes(q)
      );
    }
    this.filteredConversations = list;
  }

  setFilter(filter: 'ALL' | 'ACTIVE' | 'PENDING' | 'WAITING'): void {
    this.selectedFilter = filter;
    this.applyFilter();
  }

  onSearchChange(): void {
    this.applyFilter();
  }

  openChat(conversation: Conversation): void {
    this.selectedConversationId = conversation.id;
    this.router.navigate(['/agent/chat', conversation.id]);
  }

  acceptConversation(event: Event, conversationId: string): void {
    event.stopPropagation();
    const user = this.authService.getCurrentUser();
    if (!user) return;
    this.apiService.assignConversation(conversationId, user.id).pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.loadConversations();
    });
  }

  getStatusLabel(status: ConversationStatus): string {
    const map: Record<string, string> = {
      PENDING: 'Pending',
      ASSIGNED: 'Assigned',
      ACTIVE: 'Active',
      WAITING: 'Waiting',
      RESOLVED: 'Resolved',
      CLOSED: 'Closed'
    };
    return map[status] ?? status;
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }

  countByStatus(status: string): number {
    return this.conversations.filter(c => c.status === status).length;
  }

  getWaitingTime(conv: Conversation): string {
    if (!conv.lastMessageAt) return '';
    const diff = Math.floor((Date.now() - new Date(conv.lastMessageAt).getTime()) / 60000);
    if (diff < 60) return `${diff}m`;
    return `${Math.floor(diff / 60)}h ${diff % 60}m`;
  }
}
