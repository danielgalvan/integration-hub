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
import { MatSelectModule } from '@angular/material/select';
import { AuthApiService } from '../../../core/auth/auth-api.service';
import { Environment } from '../../../core/models/auth.models';
import { SessionService } from '../../../core/auth/session.service';

@Component({
  selector: 'ihub-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, MatSelectModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly api = inject(AuthApiService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  readonly environments = signal<Environment[]>([]);
  readonly loadingEnvironments = signal(true);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    environment: ['', Validators.required],
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  constructor() {
    this.api.environments().subscribe({
      next: environments => {
        this.environments.set(environments);
        if (environments[0]) this.form.controls.environment.setValue(environments[0].id);
        if (!environments.length) this.error.set('Nenhum ambiente está disponível para acesso.');
        this.loadingEnvironments.set(false);
      },
      error: error => {
        this.error.set(error.error?.message || 'Não foi possível carregar os ambientes.');
        this.loadingEnvironments.set(false);
      },
    });
  }

  submit(): void {
    if (this.form.invalid || this.loading()) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    const selected = this.environments().find(environment => environment.id === value.environment);
    if (!selected) { this.error.set('Selecione um ambiente válido.'); return; }
    this.loading.set(true);
    this.error.set('');
    this.api.login(value.username.trim(), value.password, selected.id).subscribe({
      next: response => {
        this.session.save(response, selected.id, selected.name);
        void this.router.navigateByUrl(response.passwordChangeRequired ? '/change-password' : '/integrations');
      },
      error: error => {
        this.error.set(error.error?.message || 'Não foi possível realizar o login. Verifique usuário, senha e ambiente.');
        this.loading.set(false);
      },
    });
  }
}
