import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  dateFrom = this.formatDate(new Date(Date.now() - 7 * 86400000));
  dateTo   = this.formatDate(new Date());
  loading  = false;

  summary = {
    totalConversations: 158,
    resolved: 142,
    slaRate: 92,
    avgHandlingTime: 9,
    avgWaitingTime: 3,
    customerSatisfaction: 4.2
  };

  topAgents = [
    { name: 'Nguyễn Văn Agent', resolved: 45, slaRate: 97, avgTime: 7 },
    { name: 'Trần Thị Hoa',     resolved: 38, slaRate: 95, avgTime: 8 },
    { name: 'Lê Quang Minh',    resolved: 32, slaRate: 90, avgTime: 11 },
  ];

  channelStats = [
    { channel: 'WEB',      count: 82, pct: 52 },
    { channel: 'MOBILE',   count: 44, pct: 28 },
    { channel: 'FACEBOOK', count: 20, pct: 13 },
    { channel: 'ZALO',     count: 12, pct: 7  },
  ];

  dailyTrend = [
    { day: 'Mon', total: 28, resolved: 25 },
    { day: 'Tue', total: 22, resolved: 20 },
    { day: 'Wed', total: 31, resolved: 28 },
    { day: 'Thu', total: 18, resolved: 17 },
    { day: 'Fri', total: 35, resolved: 30 },
    { day: 'Sat', total: 14, resolved: 12 },
    { day: 'Sun', total: 10, resolved: 10 },
  ];

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    // TODO: Load from API when implemented
  }

  private formatDate(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  get maxDailyTotal(): number {
    return Math.max(...this.dailyTrend.map(d => d.total));
  }
}
