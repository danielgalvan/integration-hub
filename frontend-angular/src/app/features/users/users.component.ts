import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { User, UserCreateResponse } from '../../core/models';
import { ConfirmationService } from '../../shared/dialogs/confirmation.service';
import { SecretDialogComponent } from '../../shared/dialogs/secret-dialog.component';
import { UsersApiService } from './users-api.service';
import { UserFormComponent } from './components/user-form.component';
import { UserListComponent } from './components/user-list.component';

@Component({
  standalone: true,
  imports: [UserFormComponent, UserListComponent, MatButtonModule, MatDialogModule, MatIconModule, MatProgressBarModule, MatSnackBarModule],
  template: `
    <section class="page users-page">
      @if (formOpen()) {
        <header class="page-header"><div>
          <button mat-button type="button" (click)="closeForm()"><mat-icon>arrow_back</mat-icon> Usuários</button>
          <p class="eyebrow">ADMINISTRAÇÃO</p><h2>{{ selected() ? 'Editar usuário' : 'Novo usuário' }}</h2>
        </div></header>
        <ihub-user-form [user]="selected()" [saving]="saving()" [error]="formError()" (saved)="save($event)" (cancel)="closeForm()" />
      } @else {
        <header class="page-header"><div><p class="eyebrow">ADMINISTRAÇÃO</p><h2>Usuários</h2><p class="description">Gerencie usuários, perfis de acesso e credenciais do Integration Hub.</p></div>
          <button mat-flat-button color="primary" type="button" (click)="newUser()"><mat-icon>person_add</mat-icon>Novo usuário</button>
        </header>
        @if (error()) { <div class="error-banner" role="alert"><mat-icon>error_outline</mat-icon>{{ error() }}<button mat-button type="button" (click)="error.set('')">Fechar</button></div> }
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        <ihub-user-list [users]="items()" [loading]="loading()" (edit)="editUser($event)" (resetPassword)="resetPassword($event)" (remove)="removeUser($event)" />
      }
    </section>
  `,
  styles: [`
    .users-page{display:grid;gap:18px}.eyebrow{margin:4px 0;color:var(--ihub-muted);font-size:.68rem;font-weight:800;letter-spacing:.13em}.description{margin:6px 0 0;color:var(--ihub-muted)}.error-banner{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:9px;background:var(--ihub-danger-container);color:var(--ihub-danger);font-size:.85rem}.error-banner button{margin-left:auto}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent {
  private readonly api = inject(UsersApiService);
  private readonly confirmation = inject(ConfirmationService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  readonly items = signal<User[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly formOpen = signal(false);
  readonly selected = signal<User | null>(null);
  readonly error = signal('');
  readonly formError = signal('');

  constructor() { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.users().subscribe({
      next: users => { this.items.set(users); this.loading.set(false); },
      error: error => { this.error.set(this.message(error, 'Não foi possível carregar os usuários.')); this.loading.set(false); },
    });
  }

  newUser(): void { this.selected.set(null); this.formError.set(''); this.formOpen.set(true); }
  editUser(user: User): void { this.selected.set(user); this.formError.set(''); this.formOpen.set(true); }
  closeForm(): void { this.formOpen.set(false); this.selected.set(null); this.formError.set(''); }

  save(value: Partial<User>): void {
    if (this.saving()) return;
    this.saving.set(true);
    this.formError.set('');
    const current = this.selected();
    const request: Observable<User | UserCreateResponse> = current?.id
      ? this.api.updateUser(current.id, value)
      : this.api.createUser(value);
    request.subscribe({
      next: result => {
        this.saving.set(false);
        this.closeForm();
        if ('temporaryPassword' in result) this.showTemporaryPassword(result.temporaryPassword);
        this.snack.open('Usuário salvo com sucesso.', 'Fechar', { duration: 2200 });
        this.load();
      },
      error: error => { this.saving.set(false); this.formError.set(this.message(error, 'Não foi possível salvar o usuário.')); },
    });
  }

  resetPassword(user: User): void {
    this.confirmation.confirm({
      title: 'Redefinir senha?',
      message: `A senha de “${user.username}” será redefinida e uma nova senha temporária será gerada.`,
      confirmLabel: 'Redefinir senha',
      destructive: true,
    }).subscribe(confirmed => {
      if (!confirmed) return;
      this.api.resetPassword(user.id).subscribe({
        next: result => { this.showTemporaryPassword(result.temporaryPassword); this.snack.open('Senha redefinida.', 'Fechar', { duration: 1800 }); this.load(); },
        error: error => this.error.set(this.message(error, 'Não foi possível redefinir a senha.')),
      });
    });
  }

  removeUser(user: User): void {
    this.confirmation.confirm({
      title: 'Excluir usuário?',
      message: `O usuário “${user.username}” será removido permanentemente. Essa ação não pode ser desfeita.`,
      confirmLabel: 'Excluir usuário',
      destructive: true,
    }).subscribe(confirmed => {
      if (!confirmed) return;
      this.api.deleteUser(user.id).subscribe({
        next: () => { this.snack.open('Usuário excluído.', 'Fechar', { duration: 1800 }); this.load(); },
        error: error => this.error.set(this.message(error, 'Não foi possível excluir o usuário.')),
      });
    });
  }

  private showTemporaryPassword(secret: string): void {
    this.dialog.open(SecretDialogComponent, {
      width: 'min(560px, 94vw)',
      data: {
        title: 'Senha temporária',
        description: 'Compartilhe esta senha com o usuário por um canal seguro.',
        warning: 'Ela será exibida somente agora. No primeiro acesso, o usuário deverá cadastrar uma nova senha.',
        secret,
        label: 'Senha temporária',
      },
    });
  }

  private message(error: { error?: { message?: string }; status?: number }, fallback: string): string {
    if (error.status === 403) return 'Você não possui permissão para realizar esta operação.';
    return error.error?.message || fallback;
  }
}
