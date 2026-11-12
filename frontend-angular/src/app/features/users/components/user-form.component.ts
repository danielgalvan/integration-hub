import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { User, UserRole } from '../../../core/models';

@Component({
  selector: 'ihub-user-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule],
  template: `
    <mat-card class="form-card"><mat-card-header><mat-card-title>{{ user ? 'Editar usuário' : 'Novo usuário' }}</mat-card-title><mat-card-subtitle>{{ user ? 'Atualize os dados e o perfil de acesso.' : 'A senha temporária será gerada automaticamente após o cadastro.' }}</mat-card-subtitle></mat-card-header>
      <mat-card-content><form [formGroup]="form" (ngSubmit)="submit()">
        <div class="form-grid">
          <mat-form-field appearance="outline"><mat-label>Usuário</mat-label><input matInput formControlName="username" maxlength="100" autocomplete="off">@if (form.controls.username.hasError('required')) { <mat-error>Usuário é obrigatório.</mat-error> }</mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Nome</mat-label><input matInput formControlName="name" maxlength="200" autocomplete="off">@if (form.controls.name.hasError('required')) { <mat-error>Nome é obrigatório.</mat-error> }</mat-form-field>
          <mat-form-field appearance="outline"><mat-label>E-mail</mat-label><input matInput type="email" formControlName="email" maxlength="200" autocomplete="off">@if (form.controls.email.hasError('email')) { <mat-error>Informe um e-mail válido.</mat-error> }</mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Perfil</mat-label><mat-select formControlName="type"><mat-option value="A">Administrador</mat-option><mat-option value="C">Criador</mat-option><mat-option value="U">Consumidor</mat-option></mat-select></mat-form-field>
          @if (user) { <mat-form-field appearance="outline"><mat-label>Status</mat-label><mat-select formControlName="status"><mat-option value="A">Ativo</mat-option><mat-option value="I">Inativo</mat-option></mat-select></mat-form-field> }
        </div>
        @if (!user) { <section class="info"><mat-icon>info</mat-icon><div><strong>Senha temporária</strong><p>O sistema gera uma senha após o cadastro. No primeiro acesso, o usuário deverá alterá-la.</p></div></section> }
        @if (error) { <p class="form-error" role="alert">{{ error }}</p> }
        <div class="actions"><button mat-button type="button" [disabled]="saving" (click)="cancel.emit()">Cancelar</button><button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving"><mat-icon>{{ user ? 'save' : 'person_add' }}</mat-icon>{{ saving ? 'Salvando…' : user ? 'Salvar alterações' : 'Criar usuário' }}</button></div>
      </form></mat-card-content>
    </mat-card>
  `,
  styles: [`
    .form-card{max-width:980px;margin:0 auto}.form-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px 18px;padding-top:20px}.form-grid mat-form-field{width:100%}.info{display:flex;align-items:flex-start;gap:12px;margin:4px 0 16px;padding:13px;border-radius:9px;background:var(--ihub-info-container);color:var(--ihub-info)}.info p{margin:4px 0 0;color:var(--ihub-text-secondary);font-size:.82rem}.form-error{color:var(--ihub-danger)}.actions{display:flex;justify-content:flex-end;gap:8px;margin-top:18px}@media(max-width:680px){.form-grid{grid-template-columns:1fr}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserFormComponent implements OnChanges {
  @Input() user: User | null = null;
  @Input() saving = false;
  @Input() error = '';
  @Output() saved = new EventEmitter<Partial<User>>();
  @Output() cancel = new EventEmitter<void>();
  private readonly fb = inject(FormBuilder);
  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(100)]],
    name: ['', [Validators.required, Validators.maxLength(200)]],
    email: ['', [Validators.email, Validators.maxLength(200)]],
    type: ['U' as UserRole, Validators.required],
    status: ['A' as 'A' | 'I', Validators.required],
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['user']) this.form.reset({
      username: this.user?.username || '',
      name: this.user?.name || '',
      email: this.user?.email || '',
      type: this.user?.type || 'U',
      status: this.user?.status || 'A',
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    const payload: Partial<User> = {
      username: value.username.trim(),
      name: value.name.trim(),
      email: value.email.trim() || undefined,
      type: value.type,
    };
    if (this.user) payload.status = value.status;
    this.saved.emit(payload);
  }
}
