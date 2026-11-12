import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { authInterceptor } from '../../core/auth.interceptor';
import { Integration } from '../../core/models';
import { IntegrationsApiService } from './integrations-api.service';

const integration: Integration = {
  id: 4,
  name: 'Pedidos',
  basePath: '/api/pedidos/',
  active: 'S',
  authType: 'API_KEY',
};

describe('IntegrationsApiService', () => {
  let api: IntegrationsApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    api = TestBed.inject(IntegrationsApiService);
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
});
