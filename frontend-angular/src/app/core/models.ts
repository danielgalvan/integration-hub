export type UserRole = 'A' | 'C' | 'U';
export type ActiveFlag = 'S' | 'N';
export type AuthType = 'NONE' | 'API_KEY';
export type ParameterType = 'VARCHAR2' | 'NUMBER' | 'DATE' | 'TIMESTAMP';

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

export interface Integration {
  id?: number;
  name: string;
  description?: string;
  basePath: string;
  active: ActiveFlag;
  authType: AuthType;
  apiKeyCreatedAt?: string;
}

export interface EndpointParameter {
  name: string;
  type: ParameterType;
  required: boolean;
}

export interface Endpoint {
  id?: number;
  integrationId: number;
  name: string;
  description?: string;
  path: string;
  method: 'GET';
  sqlText: string;
  parameters: EndpointParameter[];
  active: ActiveFlag;
}

export interface Credential {
  id: number;
  integrationId: number;
  name: string;
  active: ActiveFlag;
  lastUsedAt?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ApiKeyResponse {
  apiKey: string;
}

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

export interface Execution {
  id: number;
  integrationId: number;
  integrationName?: string;
  endpointId: number;
  endpointName?: string;
  credentialId?: number;
  credentialName?: string;
  requestMethod?: string;
  requestPath?: string;
  requestParameters?: string;
  startedAt: string;
  finishedAt?: string;
  durationMs?: number;
  httpStatus?: number;
  executionStatus: 'SUCCESS' | 'ERROR' | 'UNAUTHORIZED' | 'NOT_FOUND' | string;
  errorType?: string;
  errorMessage?: string;
  createdAt?: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EndpointExecutionResult {
  success: boolean;
  status: number;
  statusText: string;
  url: string;
  duration: number;
  data: unknown;
}
