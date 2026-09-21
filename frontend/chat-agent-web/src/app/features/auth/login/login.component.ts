import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';
import { WebSocketService } from '../../../core/services/websocket.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  // Tab: 'existing' = dùng agentId có sẵn | 'new' = tạo agent mới
  activeTab: 'existing' | 'new' = 'new';

  // Form tạo agent mới
  newAgentName = '';
  newAgentEmail = '';
  newAgentRole: 'AGENT' | 'SUPERVISOR' = 'AGENT';

  // Form dùng agent có sẵn
  existingAgentId = '';
  existingAgentName = '';
  existingRole: 'AGENT' | 'SUPERVISOR' = 'AGENT';

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private wsService: WebSocketService,
    private router: Router
  ) {}

  // ---- Tạo agent mới ----
  createAndLogin(): void {
    if (!this.newAgentName.trim() || !this.newAgentEmail.trim()) {
      this.errorMessage = 'Please fill in name and email.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const skills = this.newAgentRole === 'SUPERVISOR'
      ? '["ORDER_SUPPORT","PAYMENT","ACCOUNT"]'
      : '["ORDER_SUPPORT"]';

    this.apiService.createAgent(this.newAgentName, this.newAgentEmail, skills).subscribe({
      next: (agent) => {
        this.successMessage = 'Agent created successfully!';
        // Login immediately without backend status update
        this.authService.loginAsAgent(agent.agentId, agent.agentName, this.newAgentRole);
        // Connect WebSocket
        this.wsService.connect(agent.agentId, this.newAgentRole);
        setTimeout(() => this.navigate(), 500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message ?? 'Failed to create agent.';
        this.loading = false;
      }
    });
  }

  // ---- Dùng agentId có sẵn ----
  loginExisting(): void {
    if (!this.existingAgentId.trim() || !this.existingAgentName.trim()) {
      this.errorMessage = 'Please enter Agent ID and your display name.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    // In mock mode, just login directly with provided credentials
    this.authService.loginAsAgent(this.existingAgentId.trim(), this.existingAgentName.trim(), this.existingRole);
    // Connect WebSocket
    this.wsService.connect(this.existingAgentId.trim(), this.existingRole);
    setTimeout(() => this.navigate(), 500);
  }

  private navigate(): void {
    this.loading = false;
    if (this.authService.isSupervisor()) {
      this.router.navigate(['/supervisor']);
    } else {
      this.router.navigate(['/agent']);
    }
  }
}
