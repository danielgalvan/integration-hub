import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthApiService } from '../../../core/auth-api.service';
import { SessionService } from '../../../core/session.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let api: {
    environments: ReturnType<typeof vi.fn>;
    login: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    sessionStorage.clear();
    api = {
      environments: vi.fn(() => of([{ id: 'cloud', name: 'Cloud' }])),
      login: vi.fn(() => of({ token: 'signed-token', tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false })),
    };
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthApiService, useValue: api },
        provideRouter([]),
      ],
    });
  });

  it('loads environments and signs in with the selected environment', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.form.setValue({ environment: 'cloud', username: ' daniel ', password: 'secret' });
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    component.submit();

    expect(api.environments).toHaveBeenCalledOnce();
    expect(api.login).toHaveBeenCalledWith('daniel', 'secret', 'cloud');
    expect(TestBed.inject(SessionService).token()).toBe('signed-token');
    expect(navigate).toHaveBeenCalledWith('/integrations');
    fixture.destroy();
  });

  it('shows a compact login form with environment after username and password', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    const labels = Array.from(
      fixture.nativeElement.querySelectorAll('mat-label'),
      (label: Element) => label.textContent?.trim(),
    );
    const submitButton = fixture.nativeElement.querySelector('button[type="submit"]');

    expect(labels).toEqual(['Usuário', 'Senha', 'Ambiente']);
    expect(fixture.nativeElement.querySelector('.login-intro')).toBeNull();
    expect(submitButton?.textContent?.trim()).toBe('Entrar');

    fixture.destroy();
  });

  it('does not send incomplete credentials and displays login errors', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.submit();
    expect(api.login).not.toHaveBeenCalled();
    expect(component.form.controls.username.touched).toBe(true);

    api.login.mockReturnValueOnce(throwError(() => ({ error: { message: 'Credenciais inválidas' } })));
    component.form.setValue({ environment: 'cloud', username: 'daniel', password: 'wrong' });
    component.submit();

    expect(component.error()).toBe('Credenciais inválidas');
    expect(component.loading()).toBe(false);
    fixture.destroy();
  });
});
