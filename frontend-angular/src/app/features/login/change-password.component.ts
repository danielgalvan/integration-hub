import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiService } from '../../core/api.service';
import { SessionService } from '../../core/session.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangePasswordComponent {
  private readonly api = inject(ApiService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmation: ['', Validators.required],
  });

  submit(): void {
    const value = this.form.getRawValue();
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (value.password !== value.confirmation) {
      this.error.set('As senhas não conferem.');
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.api.changePassword(value.password).subscribe({
      next: () => {
        this.session.setPasswordChanged();
        void this.router.navigateByUrl('/integrations');
      },
      error: error => {
        this.error.set(error.error?.message || 'Não foi possível alterar a senha.');
        this.loading.set(false);
      },
    });
  }

  logout(): void {
    this.session.clear();
    void this.router.navigateByUrl('/login');
  }
}
