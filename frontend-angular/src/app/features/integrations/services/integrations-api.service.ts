import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiKeyResponse, Credential, Integration } from '../models/integration.models';

@Injectable({ providedIn: 'root' })
export class IntegrationsApiService {
  private readonly http = inject(HttpClient);

  integrations(): Observable<Integration[]> {
    return this.http.get<Integration[]>('/api/integrations');
  }

  createIntegration(value: Integration): Observable<Integration> {
    return this.http.post<Integration>('/api/integrations', value);
  }

  updateIntegration(id: number, value: Integration): Observable<Integration> {
    return this.http.put<Integration>(`/api/integrations/${id}`, value);
  }

  deleteIntegration(id: number): Observable<void> {
    return this.http.delete<void>(`/api/integrations/${id}`);
  }

  generateIntegrationApiKey(id: number): Observable<ApiKeyResponse> {
    return this.http.post<ApiKeyResponse>(`/api/integrations/${id}/api-key`, {});
  }

  credentials(integrationId: number): Observable<Credential[]> {
    return this.http.get<Credential[]>(`/api/integrations/${integrationId}/credentials`);
  }

  createCredential(integrationId: number, name: string): Observable<ApiKeyResponse> {
    return this.http.post<ApiKeyResponse>(`/api/integrations/${integrationId}/credentials`, { name });
  }

  updateCredential(integrationId: number, id: number, name: string): Observable<Credential> {
    return this.http.put<Credential>(`/api/integrations/${integrationId}/credentials/${id}`, { name });
  }

  setCredentialActive(integrationId: number, id: number, active: boolean): Observable<Credential> {
    return this.http.put<Credential>(`/api/integrations/${integrationId}/credentials/${id}/active`, { active });
  }

  credentialKey(integrationId: number, id: number, regenerate = false): Observable<ApiKeyResponse> {
    const url = `/api/integrations/${integrationId}/credentials/${id}/api-key`;
    return regenerate ? this.http.post<ApiKeyResponse>(url, {}) : this.http.get<ApiKeyResponse>(url);
  }
}
