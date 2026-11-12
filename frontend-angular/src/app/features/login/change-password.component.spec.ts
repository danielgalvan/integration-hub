import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { AuthApiService } from '../../core/auth-api.service';
import { SessionService } from '../../core/session.service';
import { ChangePasswordComponent } from './change-password.component';

describe('ChangePasswordComponent', () => {
  it('altera a senha e libera a navegação para integrações', () => {
    const api = { changePassword: vi.fn(() => of(void 0)) };
    const session = { setPasswordChanged: vi.fn() };
    TestBed.configureTestingModule({
      imports: [ChangePasswordComponent],
      providers: [
        provideRouter([]),
        { provide: AuthApiService, useValue: api },
        { provide: SessionService, useValue: session },
      ],
    });

    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const fixture = TestBed.createComponent(ChangePasswordComponent);
    const component = fixture.componentInstance;
    component.form.setValue({ password: 'senha-nova', confirmation: 'senha-nova' });

    component.submit();

    expect(api.changePassword).toHaveBeenCalledWith('senha-nova');
    expect(session.setPasswordChanged).toHaveBeenCalledOnce();
    expect(navigate).toHaveBeenCalledWith('/integrations');
    fixture.destroy();
  });

  it('não envia senhas diferentes e apresenta uma mensagem de erro', () => {
    const api = { changePassword: vi.fn(() => of(void 0)) };
    TestBed.configureTestingModule({
      imports: [ChangePasswordComponent],
      providers: [
        provideRouter([]),
        { provide: AuthApiService, useValue: api },
        { provide: SessionService, useValue: { setPasswordChanged: vi.fn() } },
      ],
    });

    const fixture = TestBed.createComponent(ChangePasswordComponent);
    const component = fixture.componentInstance;
    component.form.setValue({ password: 'senha-nova', confirmation: 'outra-senha' });

    component.submit();

    expect(api.changePassword).not.toHaveBeenCalled();
    expect(component.error()).toBe('As senhas não conferem.');
    fixture.destroy();
  });
});
