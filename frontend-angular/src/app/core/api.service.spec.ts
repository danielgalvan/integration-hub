import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { beforeEach, afterEach, describe, expect, it } from 'vitest';
import { ApiService } from './api.service';
import { authInterceptor, SKIP_AUTH_REDIRECT } from './auth.interceptor';
import { Endpoint, Integration } from './models';
import { SessionService } from './session.service';
import { provideRouter } from '@angular/router';

const integration: Integration = {
  id: 4,
  name: 'Pedidos',
  basePath: '/api/pedidos/',
  active: 'S',
  authType: 'API_KEY',
};

const endpoint: Endpoint = {
  id: 8,
  integrationId: 4,
  name: 'Buscar pedido',
  path: '/buscar',
  method: 'GET',
  sqlText: 'select id from pedido where id = :id',
  parameters: [],
  active: 'S',
};

describe('ApiService', () => {
  let api: ApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    api = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the integrations from the admin API', () => {
    let received: Integration[] | undefined;
    api.integrations().subscribe(value => received = value);

    const request = http.expectOne('/api/integrations');
    expect(request.request.method).toBe('GET');
    request.flush([integration]);
    expect(received).toEqual([integration]);
  });

  it('posts a new integration to the admin API', () => {
    api.createIntegration(integration).subscribe();

    const request = http.expectOne('/api/integrations');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(integration);
    request.flush(integration);
  });

  it('builds a dynamic endpoint URL with escaped non-empty query values', () => {
    expect(api.endpointUrl(integration, endpoint, { id: 'A & B', empty: '', missing: null }))
      .toBe('/api/pedidos/buscar?id=A+%26+B');
  });

  it('executes a dynamic endpoint with API key and skips admin-session redirection', () => {
    const session = TestBed.inject(SessionService);
    session.save({
      token: `header.${btoa(JSON.stringify({ role: 'A' }))}.signature`,
      tokenType: 'Bearer',
      expiresIn: 3600,
      passwordChangeRequired: false,
    }, 'cloud', 'Cloud');

    let status: number | undefined;
    api.execute(integration, endpoint, { id: '17' }, 'ihub_secret').subscribe(response => status = response.status);

    const request = http.expectOne('/api/pedidos/buscar?id=17');
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('text');
    expect(request.request.headers.get('X-API-Key')).toBe('ihub_secret');
    expect(request.request.headers.get('Authorization')).toBe('Bearer header.eyJyb2xlIjoiQSJ9.signature');
    expect(request.request.context.get(SKIP_AUTH_REDIRECT)).toBe(true);
    request.flush('{"id":17}', { status: 200, statusText: 'OK' });
    expect(status).toBe(200);
  });
});
