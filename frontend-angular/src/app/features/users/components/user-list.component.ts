import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { User, UserRole } from '../../../core/models';

@Component({
  selector: 'ihub-user-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTableModule, MatTooltipModule],
  template: `
    @if (users.length) {
      <div class="table-card"><table mat-table [dataSource]="users" class="user-table">
        <ng-container matColumnDef="username"><th mat-header-cell *matHeaderCellDef>Usuário</th><td mat-cell *matCellDef="let user"><strong>{{ user.username }}</strong></td></ng-container>
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Nome</th><td mat-cell *matCellDef="let user">{{ user.name }}</td></ng-container>
        <ng-container matColumnDef="email"><th mat-header-cell *matHeaderCellDef>E-mail</th><td mat-cell *matCellDef="let user">{{ user.email || '—' }}</td></ng-container>
        <ng-container matColumnDef="role"><th mat-header-cell *matHeaderCellDef>Perfil</th><td mat-cell *matCellDef="let user"><span class="role-chip">{{ roleLabel(user.type) }}</span></td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let user"><span class="status" [class.inactive]="user.status !== 'A'">{{ user.status === 'A' ? 'Ativo' : 'Inativo' }}</span></td></ng-container>
        <ng-container matColumnDef="password"><th mat-header-cell *matHeaderCellDef>Troca de senha</th><td mat-cell *matCellDef="let user">{{ user.passwordChangeRequired ? 'Pendente' : 'Concluída' }}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Ações</th><td mat-cell *matCellDef="let user" class="actions"><button mat-icon-button type="button" matTooltip="Editar usuário" (click)="edit.emit(user)"><mat-icon>edit</mat-icon></button><button mat-icon-button type="button" matTooltip="Redefinir senha" (click)="resetPassword.emit(user)"><mat-icon>password</mat-icon></button><button mat-icon-button color="warn" type="button" matTooltip="Excluir usuário" (click)="remove.emit(user)"><mat-icon>delete_outline</mat-icon></button></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="columns"></tr><tr mat-row *matRowDef="let row; columns: columns"></tr>
      </table></div>
    } @else if (!loading) {
      <section class="empty-card"><mat-icon>group</mat-icon><h3>Nenhum usuário cadastrado</h3><p>Cadastre os usuários e atribua perfis de acesso.</p></section>
    }
  `,
  styles: [`
    .table-card{overflow:auto;border:1px solid var(--ihub-border);border-radius:14px;background:var(--ihub-surface);box-shadow:0 2px 6px color-mix(in srgb,var(--ihub-text) 4%,transparent)}.user-table{width:100%;min-width:900px}.role-chip{padding:4px 8px;border-radius:99px;background:var(--ihub-surface-subtle);color:var(--ihub-text-secondary);font-size:.74rem;font-weight:700}.status{display:inline-block;padding:4px 9px;border-radius:99px;background:var(--ihub-success-container);color:var(--ihub-success);font-size:.74rem;font-weight:700}.status.inactive{background:var(--ihub-danger-container);color:var(--ihub-danger)}.actions{text-align:right;white-space:nowrap}.empty-card{display:grid;justify-items:center;padding:54px 20px;border:1px dashed var(--ihub-outline);border-radius:14px;background:var(--ihub-surface);text-align:center}.empty-card mat-icon{color:var(--ihub-muted);font-size:38px}.empty-card h3{margin:12px 0 4px;font-size:1rem}.empty-card p{margin:0;color:var(--ihub-muted)}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserListComponent {
  @Input() users: User[] = [];
  @Input() loading = false;
  @Output() edit = new EventEmitter<User>();
  @Output() resetPassword = new EventEmitter<User>();
  @Output() remove = new EventEmitter<User>();
  readonly columns = ['username', 'name', 'email', 'role', 'status', 'password', 'actions'];
  roleLabel(type: UserRole): string { return ({ A: 'Administrador', C: 'Criador', U: 'Consumidor' })[type]; }
}
