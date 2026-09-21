export interface User {
  id: string;
  username: string;
  fullName: string;
  email: string;
  role: 'AGENT' | 'SUPERVISOR' | 'ADMIN';
  status?: 'ONLINE' | 'AWAY' | 'OFFLINE';
  avatarUrl?: string;
}

export interface AuthToken {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: AuthToken;
  user: User;
}
