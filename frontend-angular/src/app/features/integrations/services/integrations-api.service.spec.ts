import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { authInterceptor } from '../../../core/auth/auth.interceptor';
import { Integration } from '../models/integration.models';
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

  it('updates and deletes an integration by ID', () => {
    api.updateIntegration(integration.id!, integration).subscribe();
    const update = http.expectOne(`/api/integrations/${integration.id}`);
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual(integration);
    update.flush(integration);

    api.deleteIntegration(integration.id!).subscribe();
    const deletion = http.expectOne(`/api/integrations/${integration.id}`);
    expect(deletion.request.method).toBe('DELETE');
    deletion.flush(null);
  });

  it('generates the legacy integration API key', () => {
    api.generateIntegrationApiKey(integration.id!).subscribe();
    const request = http.expectOne(`/api/integrations/${integration.id}/api-key`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush({ apiKey: 'ihub_secret' });
  });

  it('lists and creates credentials under an integration', () => {
    api.credentials(integration.id!).subscribe();
    const listing = http.expectOne(`/api/integrations/${integration.id}/credentials`);
    expect(listing.request.method).toBe('GET');
    listing.flush([]);

    api.createCredential(integration.id!, 'Relatórios').subscribe();
    const creation = http.expectOne(`/api/integrations/${integration.id}/credentials`);
    expect(creation.request.method).toBe('POST');
    expect(creation.request.body).toEqual({ name: 'Relatórios' });
    creation.flush({ apiKey: 'ihub_new_secret' });
  });

  it('updates credential details and active state', () => {
    const path = `/api/integrations/${integration.id}/credentials/12`;
    api.updateCredential(integration.id!, 12, 'Consumer').subscribe();
    const update = http.expectOne(path);
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual({ name: 'Consumer' });
    update.flush({ id: 12, integrationId: integration.id, name: 'Consumer', active: 'S' });

    api.setCredentialActive(integration.id!, 12, false).subscribe();
    const active = http.expectOne(`${path}/active`);
    expect(active.request.method).toBe('PUT');
    expect(active.request.body).toEqual({ active: false });
    active.flush({ id: 12, integrationId: integration.id, name: 'Consumer', active: 'N' });
  });

  it('retrieves and regenerates a credential API key using the existing HTTP contract', () => {
    const path = `/api/integrations/${integration.id}/credentials/12/api-key`;
    api.credentialKey(integration.id!, 12).subscribe();
    const retrieval = http.expectOne(path);
    expect(retrieval.request.method).toBe('GET');
    retrieval.flush({ apiKey: 'ihub_current' });

    api.credentialKey(integration.id!, 12, true).subscribe();
    const regeneration = http.expectOne(path);
    expect(regeneration.request.method).toBe('POST');
    expect(regeneration.request.body).toEqual({});
    regeneration.flush({ apiKey: 'ihub_replaced' });
  });
});
