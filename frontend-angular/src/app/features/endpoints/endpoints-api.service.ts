import { HttpClient, HttpContext, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SKIP_AUTH_REDIRECT } from '../../core/auth.interceptor';
import { Endpoint, Integration } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class EndpointsApiService {
  private readonly http = inject(HttpClient);

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
    values: Record<string, string | null | undefined>,
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

  endpointUrl(
    integration: Integration,
    endpoint: Endpoint,
    values: Record<string, string | null | undefined>,
  ): string {
    const basePath = integration.basePath.replace(/\/$/, '');
    const endpointPath = endpoint.path.replace(/^\//, '');
    const query = new URLSearchParams();

    Object.entries(values).forEach(([name, value]) => {
      if (value !== '' && value !== null && value !== undefined) query.append(name, value);
    });

    return `${basePath}/${endpointPath}${query.size ? `?${query.toString()}` : ''}`;
  }
}
