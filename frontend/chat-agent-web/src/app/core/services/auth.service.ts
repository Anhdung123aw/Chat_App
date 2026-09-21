import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { User } from '../models/user.model';
import { ApiService } from './api.service';

/**
 * AuthService — Dev Mode
 *
 * Backend không có endpoint login username/password.
 * Thay vào đó:
 *   - Agent tự nhập agentId (tồn tại trong CHAT_AGENT table)
 *   - Hoặc tạo agent mới rồi dùng agentId đó
 *   - Role được set thủ công: AGENT hoặc SUPERVISOR
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      try { this.currentUserSubject.next(JSON.parse(stored)); } catch { }
    }
  }

  /**
   * "Login" bằng cách lookup agentId trong backend,
   * nếu tồn tại thì set làm currentUser.
   */
  loginAsAgent(agentId: string, agentName: string, role: 'AGENT' | 'SUPERVISOR' = 'AGENT'): void {
    const user: User = {
      id:       agentId,
      username: agentId,
      fullName: agentName,
      email:    `${agentId}@chat.local`,
      role:     role,
      status:   'ONLINE'
    };
    localStorage.setItem('currentUser', JSON.stringify(user));
    // Dùng agentId làm token tạm để interceptor add header
    localStorage.setItem('accessToken', `dev-token-${agentId}`);
    this.currentUserSubject.next(user);
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  isAuthenticated(): boolean {
    return !!this.currentUserSubject.value;
  }

  isSupervisor(): boolean {
    const u = this.getCurrentUser();
    return u?.role === 'SUPERVISOR' || u?.role === 'ADMIN';
  }

  isAgent(): boolean {
    return this.getCurrentUser()?.role === 'AGENT';
  }
}
