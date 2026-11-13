import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let api: AuthApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(AuthApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads available environments', () => {
    api.environments().subscribe();
    const request = http.expectOne('/api/environments');
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 'cloud', name: 'Oracle Cloud' }]);
  });

  it('sends username, password, and selected environment to login', () => {
    api.login('admin', 'secret', 'cloud').subscribe();
    const request = http.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ username: 'admin', password: 'secret', environment: 'cloud' });
    request.flush({ token: 'jwt', tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false });
  });

  it('loads the authenticated user', () => {
    api.me().subscribe();
    const request = http.expectOne('/api/auth/me');
    expect(request.request.method).toBe('GET');
    request.flush({ id: 1, username: 'admin', name: 'Administrador', role: 'A' });
  });

  it('sends the new password to the password endpoint', () => {
    api.changePassword('new-secret').subscribe();
    const request = http.expectOne('/api/auth/password');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ newPassword: 'new-secret' });
    request.flush(null);
  });
});
