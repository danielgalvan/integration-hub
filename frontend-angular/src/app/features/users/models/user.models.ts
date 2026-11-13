import { UserRole } from '../../../core/models/common.models';

export interface User {
  id: number;
  username: string;
  name: string;
  email?: string;
  status: 'A' | 'I';
  type: UserRole;
  passwordChangeRequired: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserCreateResponse {
  user: User;
  temporaryPassword: string;
}

export interface TemporaryPasswordResponse {
  temporaryPassword: string;
}
