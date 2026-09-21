import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

interface CannedReply {
  id: string;
  title: string;
  content: string;
  category: string;
}

@Component({
  selector: 'app-canned-replies',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './canned-replies.component.html',
  styleUrl: './canned-replies.component.scss'
})
export class CannedRepliesComponent implements OnInit {
  @Output() replySelected = new EventEmitter<string>();

  replies: CannedReply[] = [];
  filteredReplies: CannedReply[] = [];
  searchQuery = '';
  loading = false;

  // Fallback demo data nếu API chưa có
  private demoReplies: CannedReply[] = [
    { id: '1', title: 'Greeting', content: 'Xin chào! Tôi có thể giúp gì cho bạn hôm nay?', category: 'General' },
    { id: '2', title: 'Thank you', content: 'Cảm ơn bạn đã liên hệ. Chúng tôi đã nhận được yêu cầu của bạn.', category: 'General' },
    { id: '3', title: 'Wait a moment', content: 'Vui lòng chờ một chút, tôi đang kiểm tra thông tin cho bạn.', category: 'Support' },
    { id: '4', title: 'Issue resolved', content: 'Vấn đề của bạn đã được giải quyết. Bạn có cần hỗ trợ thêm gì không?', category: 'Support' },
    { id: '5', title: 'Transfer', content: 'Tôi sẽ chuyển bạn đến bộ phận phù hợp hơn để hỗ trợ. Vui lòng chờ.', category: 'Escalation' },
    { id: '6', title: 'Closing', content: 'Cảm ơn bạn đã liên hệ. Chúc bạn có một ngày tốt lành!', category: 'General' },
  ];

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadReplies();
  }

  loadReplies(): void {
    this.loading = true;
    this.apiService.getCannedReplies().subscribe({
      next: (data: any[]) => {
        this.replies = data.length ? data : this.demoReplies;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.replies = this.demoReplies;
        this.applyFilter();
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    const q = this.searchQuery.toLowerCase();
    this.filteredReplies = q
      ? this.replies.filter(r =>
          r.title.toLowerCase().includes(q) ||
          r.content.toLowerCase().includes(q) ||
          r.category.toLowerCase().includes(q)
        )
      : [...this.replies];
  }

  onSearchChange(): void {
    this.applyFilter();
  }

  selectReply(reply: CannedReply): void {
    this.replySelected.emit(reply.content);
  }

  get categories(): string[] {
    return [...new Set(this.replies.map(r => r.category))];
  }
}
