import { HttpClient, HttpContext, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SKIP_AUTH_REDIRECT } from './auth.interceptor';
import {
  ApiKeyResponse,
  AuthUser,
  Credential,
  Endpoint,
  Environment,
  Execution,
  Integration,
  LoginResponse,
  Page,
  TemporaryPasswordResponse,
  User,
  UserCreateResponse,
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  environments(): Observable<Environment[]> {
    return this.http.get<Environment[]>('/api/environments');
  }

  login(username: string, password: string, environment: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { username, password, environment });
  }

  me(): Observable<AuthUser> {
    return this.http.get<AuthUser>('/api/auth/me');
  }

  changePassword(newPassword: string): Observable<void> {
    return this.http.put<void>('/api/auth/password', { newPassword });
  }

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

  endpoints(integrationId: number): Observable<Endpoint[]> {
    return this.http.get<Endpoint[]>(`/api/endpoints/integration/${integrationId}`);
  }

  createEndpoint(value: Endpoint): Observable<Endpoint> {
    return this.http.post<Endpoint>('/api/endpoints', value);
  }

  updateEndpoint(id: number, value: Endpoint): Observable<Endpoint> {
    return this.http.put<Endpoint>(`/api/endpoints/${id}`, value);
  }

  deleteEndpoint(id: number): Observable<void> {
    return this.http.delete<void>(`/api/endpoints/${id}`);
  }

  execute(
    integration: Integration,
    endpoint: Endpoint,
    values: Record<string, string>,
    apiKey = '',
  ): Observable<HttpResponse<string>> {
    const url = this.endpointUrl(integration, endpoint, values);
    const headers = apiKey ? new HttpHeaders({ 'X-API-Key': apiKey }) : undefined;
    return this.http.get(url, {
      headers,
      observe: 'response',
      responseType: 'text',
      context: new HttpContext().set(SKIP_AUTH_REDIRECT, true),
    });
  }

  endpointUrl(integration: Integration, endpoint: Endpoint, values: Record<string, string>): string {
    const basePath = integration.basePath.replace(/\/$/, '');
    const endpointPath = endpoint.path.replace(/^\//, '');
    const query = new URLSearchParams();

    Object.entries(values).forEach(([name, value]) => {
      if (value !== '' && value !== null && value !== undefined) query.append(name, value);
    });

    const url = `${basePath}/${endpointPath}${query.size ? `?${query.toString()}` : ''}`;
    return url;
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

  users(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }

  createUser(value: Partial<User>): Observable<UserCreateResponse> {
    return this.http.post<UserCreateResponse>('/api/users', value);
  }

  updateUser(id: number, value: Partial<User>): Observable<User> {
    return this.http.put<User>(`/api/users/${id}`, value);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`/api/users/${id}`);
  }

  resetPassword(id: number): Observable<TemporaryPasswordResponse> {
    return this.http.post<TemporaryPasswordResponse>(`/api/users/${id}/reset-password`, {});
  }

  executions(filters: Record<string, string | number>): Observable<Page<Execution>> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) params = params.set(key, String(value));
    });
    return this.http.get<Page<Execution>>('/api/admin/executions', { params });
  }

  execution(id: number): Observable<Execution> {
    return this.http.get<Execution>(`/api/admin/executions/${id}`);
  }
}
