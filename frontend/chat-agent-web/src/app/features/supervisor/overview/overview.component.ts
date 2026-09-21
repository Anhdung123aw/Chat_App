import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, interval } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { WebSocketMessageType } from '../../../core/models/websocket.model';

interface DashboardStats {
  totalActive: number;
  totalPending: number;
  totalResolved: number;
  avgWaitingTime: number;
  avgHandlingTime: number;
  agentsOnline: number;
  agentsBusy: number;
  agentsOffline: number;
  slaBreached: number;
  slaMet: number;
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss'
})
export class OverviewComponent implements OnInit, OnDestroy {
  stats: DashboardStats = {
    totalActive: 0, totalPending: 0, totalResolved: 0,
    avgWaitingTime: 0, avgHandlingTime: 0,
    agentsOnline: 0, agentsBusy: 0, agentsOffline: 0,
    slaBreached: 0, slaMet: 0
  };
  loading = false;
  lastUpdated = new Date();
  private destroy$ = new Subject<void>();

  constructor(private apiService: ApiService, private wsService: WebSocketService) {}

  ngOnInit(): void {
    this.loadStats();
    // Refresh mỗi 30 giây
    interval(30000).pipe(takeUntil(this.destroy$)).subscribe(() => this.loadStats());
    // Update khi nhận WS event
    this.wsService.getMessages().pipe(takeUntil(this.destroy$)).subscribe(msg => {
      if ([
        WebSocketMessageType.CONVERSATION_ASSIGNED,
        WebSocketMessageType.CONVERSATION_STATUS_CHANGED,
        WebSocketMessageType.USER_ONLINE,
        WebSocketMessageType.USER_OFFLINE
      ].includes(msg.type)) {
        this.loadStats();
      }
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  loadStats(): void {
    this.loading = true;
    this.apiService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = { ...this.stats, ...data };
        this.lastUpdated = new Date();
        this.loading = false;
      },
      error: () => {
        // Demo data nếu API chưa ready
        this.stats = {
          totalActive: 12, totalPending: 5, totalResolved: 48,
          avgWaitingTime: 3, avgHandlingTime: 8,
          agentsOnline: 7, agentsBusy: 5, agentsOffline: 2,
          slaBreached: 2, slaMet: 46
        };
        this.lastUpdated = new Date();
        this.loading = false;
      }
    });
  }

  get slaRate(): number {
    const total = this.stats.slaMet + this.stats.slaBreached;
    return total > 0 ? Math.round((this.stats.slaMet / total) * 100) : 100;
  }

  formatTime(minutes: number): string {
    if (minutes < 60) return `${minutes}m`;
    return `${Math.floor(minutes / 60)}h ${minutes % 60}m`;
  }

  formatLastUpdated(): string {
    return this.lastUpdated.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }
}
