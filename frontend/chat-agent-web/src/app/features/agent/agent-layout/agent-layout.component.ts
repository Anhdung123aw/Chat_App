import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { User } from '../../../core/models/user.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-agent-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './agent-layout.component.html',
  styleUrl: './agent-layout.component.scss'
})
export class AgentLayoutComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  wsConnected = false;

  constructor(
    private authService: AuthService,
    private wsService: WebSocketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        // Connect WebSocket when layout loads if already logged in (e.g., page refresh)
        this.wsService.connect(user.id, user.role as 'AGENT' | 'SUPERVISOR');
      }
    });

    this.wsService.isConnected().subscribe(connected => {
      this.wsConnected = connected;
    });
  }

  ngOnDestroy(): void {
    // Cleanup if needed
  }

  logout(): void {
    this.wsService.disconnect();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
