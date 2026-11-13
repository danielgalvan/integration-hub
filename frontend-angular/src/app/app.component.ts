import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthApiService } from './core/auth/auth-api.service';
import { SessionService } from './core/auth/session.service';
import { ThemeService } from './core/services/theme.service';
import { ShellComponent } from './layout/shell.component';

@Component({
  selector: 'ihub-root',
  standalone: true,
  imports: [RouterOutlet, ShellComponent],
  template: `
    @if (!session.token()) {
      <router-outlet />
    } @else if (session.validationStatus() === 'validating') {
      <main class="session-state" role="status" aria-live="polite">
        <span class="spinner" aria-hidden="true"></span>
        <h1>Verificando sessão</h1>
        <p>Aguarde enquanto confirmamos sua identidade.</p>
      </main>
    } @else if (session.validationStatus() === 'unavailable') {
      <main class="session-state" role="alert">
        <h1>Não foi possível validar sua sessão</h1>
        <p>{{ session.validationError() }}</p>
        <div class="session-actions">
          <button type="button" (click)="retrySessionValidation()">Tentar novamente</button>
          <button type="button" class="secondary" (click)="endSession()">Encerrar sessão</button>
        </div>
      </main>
    } @else if (isPublic()) {
      <router-outlet />
    } @else {
      <ihub-shell><router-outlet /></ihub-shell>
    }
  `,
  styles: [`
    .session-state{display:grid;justify-items:center;align-content:center;gap:12px;min-height:100vh;padding:24px;text-align:center;color:var(--ihub-text)}
    .session-state h1,.session-state p{margin:0}.session-state p{max-width:540px;color:var(--ihub-muted)}
    .spinner{width:36px;height:36px;border:4px solid var(--ihub-border);border-top-color:var(--mat-sys-primary);border-radius:50%;animation:spin .8s linear infinite}
    .session-actions{display:flex;flex-wrap:wrap;justify-content:center;gap:10px;margin-top:8px}.session-actions button{min-height:40px;padding:0 16px;border:0;border-radius:8px;background:var(--mat-sys-primary);color:var(--mat-sys-on-primary);font:inherit;font-weight:600;cursor:pointer}.session-actions button.secondary{border:1px solid var(--ihub-border);background:transparent;color:inherit}
    @keyframes spin{to{transform:rotate(360deg)}}
    @media(prefers-reduced-motion:reduce){.spinner{animation-duration:2s}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
  private readonly router = inject(Router);
  private readonly api = inject(AuthApiService);
  private readonly session = inject(SessionService);
  private readonly currentUrl = signal(this.router.url);
  private readonly validationAttempt = signal(0);

  readonly isPublic = computed(() =>
    !this.session.authenticated()
    || this.session.passwordChangeRequired()
    || this.currentUrl().startsWith('/login')
    || this.currentUrl().startsWith('/change-password'),
  );

  constructor() {
    inject(ThemeService);
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      takeUntilDestroyed(),
    ).subscribe(event => this.currentUrl.set(event.urlAfterRedirects));

    effect(onCleanup => {
      const token = this.session.token();
      this.validationAttempt();
      if (!token) {
        this.session.setValidationStatus('unauthenticated');
        return;
      }

      this.session.setValidationStatus('validating');
      const subscription = this.api.me().subscribe({
        next: user => {
          this.session.setUser(user);
          this.session.setValidationStatus('authenticated');
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 401) {
            this.session.clear();
            return;
          }
          this.session.setValidationStatus(
            'unavailable',
            'O servidor não confirmou a sessão. Verifique a conexão e tente novamente.',
          );
        },
      });
      onCleanup(() => subscription.unsubscribe());
    });
  }

  retrySessionValidation(): void {
    this.session.setValidationStatus('validating');
    this.validationAttempt.update(attempt => attempt + 1);
  }

  endSession(): void {
    this.session.clear();
    void this.router.navigateByUrl('/login');
  }
}
