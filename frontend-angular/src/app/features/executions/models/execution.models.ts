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
