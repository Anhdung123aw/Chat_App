export interface Conversation {
  id: string;
  customerId: string;
  customerName: string;
  agentId?: string;
  agentName?: string;
  status: ConversationStatus;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  topicCode?: string;
  channel: 'WEB' | 'MOBILE' | 'FACEBOOK' | 'ZALO';
  createdAt: string;
  updatedAt: string;
  lastMessageAt?: string;
  waitingTime?: number;
  unreadCount?: number;
}

export enum ConversationStatus {
  PENDING = 'PENDING',
  ASSIGNED = 'ASSIGNED',
  ACTIVE = 'ACTIVE',
  WAITING = 'WAITING',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

export interface ConversationListResponse {
  content: Conversation[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
