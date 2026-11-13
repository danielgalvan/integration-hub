import { ActiveFlag, AuthType } from '../../../core/models/common.models';

export interface Integration {
  id?: number;
  name: string;
  description?: string;
  basePath: string;
  active: ActiveFlag;
  authType: AuthType;
  apiKeyCreatedAt?: string;
}

export type PersistedIntegration = Integration & { id: number };

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

export interface CredentialDraft {
  id?: number;
  name: string;
}

export interface ApiKeyResponse {
  apiKey: string;
}
