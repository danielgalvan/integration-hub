import { Component } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { afterEach, describe, expect, it } from 'vitest';
import { AppComponent } from './app.component';
import { authGuard } from './core/auth/auth.guard';
import { authInterceptor } from './core/auth/auth.interceptor';
import { ShellComponent } from './layout/shell.component';

@Component({ selector: 'ihub-test-integrations-route', standalone: true, template: '<p>Integrações protegidas pela rota</p>' })
class IntegrationsRouteStub {}

@Component({ selector: 'ihub-test-change-password-route', standalone: true, template: '<p>Troca obrigatória de senha</p>' })
class ChangePasswordRouteStub {}

@Component({ selector: 'ihub-test-login-route', standalone: true, template: '<p>Tela de login</p>' })
class LoginRouteStub {}

describe('fluxo integrado de sessão e rotas', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [
        AppComponent,
        LoginRouteStub,
        ChangePasswordRouteStub,
        IntegrationsRouteStub,
      ],
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'login', component: LoginRouteStub },
          {
            path: 'change-password',
            component: ChangePasswordRouteStub,
            canActivate: [authGuard],
          },
          {
            path: 'integrations',
            component: IntegrationsRouteStub,
            canActivate: [authGuard],
          },
        ]),
      ],
    });
    TestBed.overrideComponent(ShellComponent, {
      set: {
        template: '<section data-testid="protected-shell"><ng-content /></section>',
        styles: [],
      },
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('aguarda a validação de /me antes de ativar uma rota protegida', async () => {
    sessionStorage.setItem('ihub_token', 'saved-jwt');
    sessionStorage.setItem('ihub_role', 'A');
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Verificando sessão');
    expect(fixture.nativeElement.querySelector('[data-testid="protected-shell"]')).toBeNull();

    const request = http.expectOne('/api/auth/me');
    expect(request.request.headers.get('Authorization')).toBe('Bearer saved-jwt');
    const navigation = TestBed.inject(Router).navigateByUrl('/integrations');
    request.flush({ id: 12, username: 'daniel', name: 'Daniel', role: 'A' });

    await expect(navigation).resolves.toBe(true);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-testid="protected-shell"]')).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Integrações protegidas pela rota');
    fixture.destroy();
  });

  it('encaminha para troca de senha obrigatória depois de validar a sessão', async () => {
    sessionStorage.setItem('ihub_token', 'saved-jwt');
    sessionStorage.setItem('ihub_role', 'U');
    sessionStorage.setItem('ihub_password_change_required', 'true');
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const request = http.expectOne('/api/auth/me');
    const router = TestBed.inject(Router);
    const navigation = router.navigateByUrl('/integrations');
    request.flush({ id: 19, username: 'usuario', name: 'Usuário', role: 'U' });

    await expect(navigation).resolves.toBe(true);
    fixture.detectChanges();

    expect(router.url).toBe('/change-password');
    expect(fixture.nativeElement.textContent).toContain('Troca obrigatória de senha');
    expect(fixture.nativeElement.querySelector('[data-testid="protected-shell"]')).toBeNull();
    fixture.destroy();
  });
});
