import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatMessage, SendMessageRequest, CreateConversationRequest, ConversationResponse } from '../models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly CORE_URL = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  /**
   * Tạo cuộc trò chuyện mới cho Khách hàng
   */
  createConversation(request: CreateConversationRequest): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(`${this.CORE_URL}/conversations`, request);
  }

  getConversation(conversationId: string): Observable<ConversationResponse> {
    return this.http.get<ConversationResponse>(`${this.CORE_URL}/conversations/${conversationId}`);
  }

  /**
   * Lấy lịch sử tin nhắn của cuộc trò chuyện
   */
  getMessages(conversationId: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.CORE_URL}/conversations/${conversationId}/messages`);
  }

  /**
   * Gửi tin nhắn REST API
   */
  sendMessage(request: any): Observable<any> {
    return this.http.post(`${this.CORE_URL}/conversations/${request.conversationId}/messages`, request);
  }

  /**
   * Đánh giá chất lượng dịch vụ (CSAT Rating)
   */
  submitRating(conversationId: string, rating: number, comment: string = ''): Observable<any> {
    return this.http.post(`${this.CORE_URL}/ratings/conversations/${conversationId}`, {
      rating,
      comment
    });
  }

  /**
   * Lấy đánh giá của một conversation
   */
  getRatingByConversation(conversationId: string): Observable<any> {
    return this.http.get(`${this.CORE_URL}/ratings/conversations/${conversationId}`);
  }
}
