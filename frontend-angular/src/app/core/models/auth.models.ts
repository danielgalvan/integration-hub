import { UserRole } from './common.models';

export interface Environment {
  id: string;
  name: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  passwordChangeRequired: boolean;
}

export interface AuthUser {
  id: number;
  username: string;
  name: string;
  email?: string | null;
  role: UserRole;
}
