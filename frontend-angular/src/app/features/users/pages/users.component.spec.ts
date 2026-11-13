import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { User } from '../models/user.models';
import { ConfirmationService } from '../../../shared/dialogs/confirmation.service';
import { SecretDialogComponent } from '../../../shared/dialogs/secret-dialog.component';
import { UsersComponent } from './users.component';
import { UsersApiService } from '../services/users-api.service';

const user: User = {
  id: 3,
  username: 'criador',
  name: 'Criador de APIs',
  email: 'criador@example.com',
  status: 'A',
  type: 'C',
  passwordChangeRequired: false,
};

describe('UsersComponent', () => {
  it('carrega os usuários e exibe o perfil e o estado da troca de senha', () => {
    const api = {
      users: vi.fn(() => of([user])),
      createUser: vi.fn(() => of({ user, temporaryPassword: 'temporary' })),
      updateUser: vi.fn(() => of(user)),
      resetPassword: vi.fn(() => of({ temporaryPassword: 'temporary' })),
      deleteUser: vi.fn(() => of(void 0)),
    };
    const dialog = { open: vi.fn() };
    const snack = { open: vi.fn() };
    const confirmation = { confirm: vi.fn(() => of(false)) };
    TestBed.configureTestingModule({
      imports: [UsersComponent],
      providers: [
        { provide: UsersApiService, useValue: api },
        { provide: MatDialog, useValue: { open: vi.fn() } },
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
        { provide: ConfirmationService, useValue: { confirm: vi.fn(() => of(false)) } },
      ],
    });
    TestBed.overrideComponent(UsersComponent, {
      set: {
        providers: [
          { provide: MatDialog, useValue: dialog },
          { provide: MatSnackBar, useValue: snack },
          { provide: ConfirmationService, useValue: confirmation },
        ],
      },
    });

    const fixture = TestBed.createComponent(UsersComponent);
    fixture.detectChanges();

    expect(api.users).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.textContent).toContain('criador');
    expect(fixture.nativeElement.textContent).toContain('Criador');
    expect(fixture.nativeElement.textContent).toContain('Concluída');
    fixture.destroy();
  });

  it('cadastra usuário, exibe a senha temporária uma vez e recarrega a lista', () => {
    const api = {
      users: vi.fn(() => of([])),
      createUser: vi.fn(() => of({ user, temporaryPassword: 'senha-temporaria' })),
      updateUser: vi.fn(() => of(user)),
      resetPassword: vi.fn(() => of({ temporaryPassword: 'senha-temporaria' })),
      deleteUser: vi.fn(() => of(void 0)),
    };
    const dialog = { open: vi.fn() };
    const snack = { open: vi.fn() };
    const confirmation = { confirm: vi.fn(() => of(false)) };
    TestBed.configureTestingModule({
      imports: [UsersComponent],
      providers: [
        { provide: UsersApiService, useValue: api },
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
        { provide: ConfirmationService, useValue: { confirm: vi.fn(() => of(false)) } },
      ],
    });
    TestBed.overrideComponent(UsersComponent, {
      set: {
        providers: [
          { provide: MatDialog, useValue: dialog },
          { provide: MatSnackBar, useValue: snack },
          { provide: ConfirmationService, useValue: confirmation },
        ],
      },
    });

    const fixture = TestBed.createComponent(UsersComponent);
    const component = fixture.componentInstance;
    const value = {
      username: 'criador',
      name: 'Criador de APIs',
      type: 'C' as const,
    };

    component.newUser();
    component.save(value);

    expect(api.createUser).toHaveBeenCalledWith(value);
    expect(api.users).toHaveBeenCalledTimes(2);
    expect(dialog.open).toHaveBeenCalledWith(
      SecretDialogComponent,
      expect.objectContaining({
        data: expect.objectContaining({
          secret: 'senha-temporaria',
          title: 'Senha temporária',
        }),
      }),
    );
    expect(component.formOpen()).toBe(false);
    fixture.destroy();
  });
});
