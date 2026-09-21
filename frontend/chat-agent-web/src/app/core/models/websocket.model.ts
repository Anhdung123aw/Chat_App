export interface WebSocketMessage {
  type: WebSocketMessageType;
  eventId: string;
  conversationId?: string;
  sequence?: number;
  timestamp: string;
  payload?: any;
  senderId?: string;
  senderType?: string;
  errorMessage?: string;
}

export enum WebSocketMessageType {
  // Message events
  MESSAGE = 'MESSAGE',
  MESSAGE_ACK = 'MESSAGE_ACK',
  
  // Conversation events
  CONVERSATION_ASSIGNED = 'CONVERSATION_ASSIGNED',
  CONVERSATION_STATUS_CHANGED = 'CONVERSATION_STATUS_CHANGED',
  CONVERSATION_CLOSED = 'CONVERSATION_CLOSED',
  
  // Subscription events
  SUBSCRIBED = 'SUBSCRIBED',
  UNSUBSCRIBED = 'UNSUBSCRIBED',
  
  // Typing events
  TYPING_START = 'TYPING_START',
  TYPING_STOP = 'TYPING_STOP',
  
  // Presence events
  USER_ONLINE = 'USER_ONLINE',
  USER_OFFLINE = 'USER_OFFLINE',
  PRESENCE_UPDATE = 'PRESENCE_UPDATE',
  
  // System events
  PONG = 'PONG',
  ERROR = 'ERROR'
}

export interface TypingIndicator {
  conversationId: string;
  userId: string;
  userName?: string;
  isTyping: boolean;
}

export interface PresenceStatus {
  userId: string;
  status: 'ONLINE' | 'AWAY' | 'OFFLINE';
  lastSeen?: string;
}
