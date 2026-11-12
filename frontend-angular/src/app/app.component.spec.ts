import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { AppComponent } from './app.component';
import { authInterceptor } from './core/auth.interceptor';
import { SessionService } from './core/session.service';
import { ShellComponent } from './layout/shell.component';

describe('AppComponent session gate', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    sessionStorage.setItem('ihub_token', 'stored-jwt');
    TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    TestBed.overrideComponent(ShellComponent, {
      set: { template: '<section data-testid="authenticated-shell"><ng-content /></section>', styles: [] },
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('does not render the protected shell until /me validates the stored token', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Verificando sessão');
    expect(fixture.nativeElement.querySelector('[data-testid="authenticated-shell"]')).toBeNull();
    const request = http.expectOne('/api/auth/me');
    expect(request.request.headers.get('Authorization')).toBe('Bearer stored-jwt');
    request.flush({ id: 7, username: 'daniel', name: 'Daniel', role: 'A' });

    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[data-testid="authenticated-shell"]')).not.toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Verificando sessão');
    fixture.destroy();
  });

  it('keeps protected pages locked after a server failure and allows retry', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    http.expectOne('/api/auth/me').flush({ message: 'Unavailable' }, { status: 503, statusText: 'Service Unavailable' });

    fixture.detectChanges();
    const session = TestBed.inject(SessionService);
    expect(session.token()).toBe('stored-jwt');
    expect(session.validationStatus()).toBe('unavailable');
    expect(fixture.nativeElement.textContent).toContain('Não foi possível validar sua sessão');
    expect(fixture.nativeElement.querySelector('[data-testid="authenticated-shell"]')).toBeNull();

    fixture.componentInstance.retrySessionValidation();
    fixture.detectChanges();
    const retry = http.expectOne('/api/auth/me');
    retry.flush({ id: 7, username: 'daniel', name: 'Daniel', role: 'A' });

    fixture.detectChanges();
    expect(session.validationStatus()).toBe('authenticated');
    expect(fixture.nativeElement.querySelector('[data-testid="authenticated-shell"]')).not.toBeNull();
    fixture.destroy();
  });
});
