import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { WebSocketMessageType } from '../../../core/models/websocket.model';

interface AgentInfo {
  id: string;
  fullName: string;
  username: string;
  email: string;
  presenceStatus: 'ONLINE' | 'AWAY' | 'OFFLINE';
  activeConversations: number;
  resolvedToday: number;
  avgHandlingTime?: number;
}

@Component({
  selector: 'app-agent-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agent-list.component.html',
  styleUrl: './agent-list.component.scss'
})
export class AgentListComponent implements OnInit, OnDestroy {
  agents: AgentInfo[] = [];
  filteredAgents: AgentInfo[] = [];
  searchQuery = '';
  filterStatus: 'ALL' | 'ONLINE' | 'AWAY' | 'OFFLINE' = 'ALL';
  loading = false;
  private destroy$ = new Subject<void>();

  constructor(private apiService: ApiService, private wsService: WebSocketService) {}

  ngOnInit(): void {
    this.loadAgents();
    interval(30000).pipe(takeUntil(this.destroy$)).subscribe(() => this.loadAgents());
    this.wsService.getMessages().pipe(takeUntil(this.destroy$)).subscribe(msg => {
      if (msg.type === WebSocketMessageType.USER_ONLINE || msg.type === WebSocketMessageType.USER_OFFLINE || msg.type === WebSocketMessageType.PRESENCE_UPDATE) {
        this.updatePresence(msg.senderId!, msg.type);
      }
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  loadAgents(): void {
    this.loading = true;
    this.apiService.getAllAgents().subscribe({
      next: (agents: any[]) => {
        const ids = agents.map(a => a.id);
        this.apiService.getBulkPresence(ids).subscribe({
          next: (presence: any) => {
            this.agents = agents.map(a => ({
              ...a,
              presenceStatus: presence[a.id]?.status ?? 'OFFLINE',
              activeConversations: a.activeConversations ?? 0,
              resolvedToday: a.resolvedToday ?? 0
            }));
            this.applyFilter();
            this.loading = false;
          },
          error: () => {
            this.agents = agents.map(a => ({ ...a, presenceStatus: 'OFFLINE', activeConversations: 0, resolvedToday: 0 }));
            this.applyFilter();
            this.loading = false;
          }
        });
      },
      error: () => {
        // Demo data
        this.agents = [
          { id: '1', fullName: 'Nguyễn Văn Agent', username: 'agent1', email: 'agent1@example.com', presenceStatus: 'ONLINE', activeConversations: 3, resolvedToday: 12 },
          { id: '2', fullName: 'Trần Thị Hoa', username: 'agent2', email: 'agent2@example.com', presenceStatus: 'ONLINE', activeConversations: 1, resolvedToday: 8 },
          { id: '3', fullName: 'Lê Quang Minh', username: 'agent3', email: 'agent3@example.com', presenceStatus: 'AWAY', activeConversations: 0, resolvedToday: 5 },
          { id: '4', fullName: 'Phạm Thị Lan', username: 'agent4', email: 'agent4@example.com', presenceStatus: 'OFFLINE', activeConversations: 0, resolvedToday: 0 },
        ];
        this.applyFilter();
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    let list = [...this.agents];
    if (this.filterStatus !== 'ALL') list = list.filter(a => a.presenceStatus === this.filterStatus);
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      list = list.filter(a => a.fullName.toLowerCase().includes(q) || a.username.toLowerCase().includes(q));
    }
    this.filteredAgents = list;
  }

  setFilter(status: 'ALL' | 'ONLINE' | 'AWAY' | 'OFFLINE'): void {
    this.filterStatus = status;
    this.applyFilter();
  }

  updatePresence(userId: string, type: WebSocketMessageType): void {
    const agent = this.agents.find(a => a.id === userId);
    if (agent) {
      if (type === WebSocketMessageType.USER_ONLINE) agent.presenceStatus = 'ONLINE';
      else if (type === WebSocketMessageType.USER_OFFLINE) agent.presenceStatus = 'OFFLINE';
      this.applyFilter();
    }
  }

  countByStatus(status: string): number {
    return this.agents.filter(a => a.presenceStatus === status).length;
  }
}
