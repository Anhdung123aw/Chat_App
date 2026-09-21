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

export interface SendMessageRequest {
  conversationId: string;
  content: string;
  messageType: MessageType;
  senderId: string;
  senderType: SenderType;
  clientMessageId: string;
}

export interface CreateConversationRequest {
  userId: string;
  topicCode?: string;
  merchantId?: string;
}

export interface ConversationResponse {
  conversationId: string;
  userId: string;
  topicCode?: string;
  status: string;
  assignedAgent?: string;
  createdAt: string;
}
