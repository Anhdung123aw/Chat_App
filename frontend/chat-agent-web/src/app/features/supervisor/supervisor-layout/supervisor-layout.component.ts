import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-supervisor-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './supervisor-layout.component.html',
  styleUrl: './supervisor-layout.component.scss'
})
export class SupervisorLayoutComponent implements OnInit {
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
        this.wsService.connect(user.id, user.role as 'AGENT' | 'SUPERVISOR');
      }
    });
    this.wsService.isConnected().subscribe(c => this.wsConnected = c);
  }

  logout(): void {
    this.wsService.disconnect();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
