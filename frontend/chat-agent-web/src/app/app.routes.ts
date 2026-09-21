import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'agent',
    loadComponent: () => import('./features/agent/agent-layout/agent-layout.component').then(m => m.AgentLayoutComponent),
    children: [
      { path: '', redirectTo: 'conversations', pathMatch: 'full' },
      {
        path: 'conversations',
        loadComponent: () => import('./features/agent/conversation-list/conversation-list.component').then(m => m.ConversationListComponent)
      },
      {
        path: 'chat/:conversationId',
        loadComponent: () => import('./features/agent/chat/chat.component').then(m => m.ChatComponent)
      }
    ]
  },
  {
    path: 'supervisor',
    loadComponent: () => import('./features/supervisor/supervisor-layout/supervisor-layout.component').then(m => m.SupervisorLayoutComponent),
    children: [
      { path: '', redirectTo: 'overview', pathMatch: 'full' },
      {
        path: 'overview',
        loadComponent: () => import('./features/supervisor/overview/overview.component').then(m => m.OverviewComponent)
      },
      {
        path: 'agents',
        loadComponent: () => import('./features/supervisor/agent-list/agent-list.component').then(m => m.AgentListComponent)
      },
      {
        path: 'queue',
        loadComponent: () => import('./features/supervisor/queue/queue.component').then(m => m.QueueComponent)
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/supervisor/reports/reports.component').then(m => m.ReportsComponent)
      }
    ]
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
