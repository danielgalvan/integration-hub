export type UserRole = 'A' | 'C' | 'U';
export type ActiveFlag = 'S' | 'N';
export type AuthType = 'NONE' | 'API_KEY';

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
