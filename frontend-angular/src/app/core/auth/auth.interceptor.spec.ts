import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { authInterceptor, SKIP_AUTH_REDIRECT } from './auth.interceptor';
import { SessionService } from './session.service';
import { HttpClient, HttpContext } from '@angular/common/http';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let session: SessionService;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
  });

  afterEach(() => controller.verify());

  it('attaches the bearer token and clears the session after a protected 401', () => {
    session.save({ token: 'jwt-token', tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false }, 'dev', 'Development');
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    http.get('/api/integrations').subscribe({ error: () => undefined });

    const request = controller.expectOne('/api/integrations');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(session.token()).toBeNull();
    expect(session.validationStatus()).toBe('unauthenticated');
    expect(navigate).toHaveBeenCalledWith('/login');
  });

  it('leaves the admin session intact for an endpoint API-key 401', () => {
    session.save({ token: 'jwt-token', tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false }, 'dev', 'Development');
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    http.get('/api/public-endpoint', {
      context: new HttpContext().set(SKIP_AUTH_REDIRECT, true),
      headers: { 'X-API-Key': 'bad-key' },
    }).subscribe({ error: () => undefined });

    const request = controller.expectOne('/api/public-endpoint');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    expect(request.request.headers.get('X-API-Key')).toBe('bad-key');
    request.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(session.token()).toBe('jwt-token');
    expect(navigate).not.toHaveBeenCalled();
  });

  it('does not redirect or clear the previous session for a rejected login attempt', () => {
    session.save({ token: 'existing-token', tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false }, 'dev', 'Development');
    session.setValidationStatus('authenticated');
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    http.post('/api/auth/login', { username: 'daniel', password: 'wrong' }).subscribe({ error: () => undefined });

    const request = controller.expectOne('/api/auth/login');
    request.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(session.token()).toBe('existing-token');
    expect(session.validationStatus()).toBe('authenticated');
    expect(navigate).not.toHaveBeenCalled();
  });
});
