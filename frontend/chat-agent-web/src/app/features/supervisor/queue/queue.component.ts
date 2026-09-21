import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { Conversation } from '../../../core/models/conversation.model';
import { WebSocketMessageType } from '../../../core/models/websocket.model';

@Component({
  selector: 'app-queue',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './queue.component.html',
  styleUrl: './queue.component.scss'
})
export class QueueComponent implements OnInit, OnDestroy {
  pending: Conversation[] = [];
  agents: any[] = [];
  selectedAgentId: { [convId: string]: string } = {};
  loading = false;
  private destroy$ = new Subject<void>();

  constructor(private apiService: ApiService, private wsService: WebSocketService) {}

  ngOnInit(): void {
    this.loadQueue();
    this.loadAgents();
    interval(20000).pipe(takeUntil(this.destroy$)).subscribe(() => this.loadQueue());
    this.wsService.getMessages().pipe(takeUntil(this.destroy$)).subscribe(msg => {
      if (msg.type === WebSocketMessageType.CONVERSATION_ASSIGNED || msg.type === WebSocketMessageType.CONVERSATION_STATUS_CHANGED) {
        this.loadQueue();
      }
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  loadQueue(): void {
    this.loading = true;
    this.apiService.getPendingConversations().subscribe({
      next: res => { this.pending = res.content; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  loadAgents(): void {
    this.apiService.getAllAgents().subscribe({
      next: agents => { this.agents = agents; },
      error: () => {
        this.agents = [
          { id: '1', fullName: 'Nguyễn Văn Agent' },
          { id: '2', fullName: 'Trần Thị Hoa' },
        ];
      }
    });
  }

  assignToAgent(conversationId: string): void {
    const agentId = this.selectedAgentId[conversationId];
    if (!agentId) return;

    this.apiService.assignConversation(conversationId, agentId)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.pending = this.pending.filter(c => c.id !== conversationId);
      });
  }

  getWaitingMinutes(conv: Conversation): number {
    const createdAt = new Date(conv.createdAt).getTime();
    return Math.floor((Date.now() - createdAt) / 60000);
  }

  isLongWaiting(conv: Conversation): boolean {
    return this.getWaitingMinutes(conv) > 5;
  }
}
