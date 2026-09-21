export interface ChatMessage {
  id: string;
  conversationId: string;
  content: string;
  messageType: MessageType;
  senderType: SenderType;
  senderId: string;
  senderName?: string;
  sequence: number;
  createdAt: string;
  status?: MessageStatus;
}

export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  FILE = 'FILE',
  SYSTEM = 'SYSTEM'
}

export enum SenderType {
  USER = 'USER',
  AGENT = 'AGENT',
  SYSTEM = 'SYSTEM'
}

export enum MessageStatus {
  SENDING = 'SENDING',
  SENT = 'SENT',
  DELIVERED = 'DELIVERED',
  READ = 'READ',
  FAILED = 'FAILED'
}

export interface SendMessageRequest {
  conversationId: string;
  content: string;
  messageType: MessageType;
  senderId: string;
  senderType: SenderType;
  clientMessageId: string;
}

export interface MessageListResponse {
  content: ChatMessage[];
  totalElements: number;
  page: number;
  size: number;
}
