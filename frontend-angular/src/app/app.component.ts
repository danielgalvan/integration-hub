import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { ApiService } from './core/api.service';
import { SessionService } from './core/session.service';
import { ShellComponent } from './layout/shell.component';

@Component({
  selector: 'ihub-root',
  standalone: true,
  imports: [RouterOutlet, ShellComponent],
  template: `
    @if (isPublic()) {
      <router-outlet />
    } @else {
      <ihub-shell><router-outlet /></ihub-shell>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);
  private readonly session = inject(SessionService);
  private readonly currentUrl = signal(this.router.url);

  readonly isPublic = computed(() =>
    !this.session.authenticated()
    || this.session.passwordChangeRequired()
    || this.currentUrl().startsWith('/login')
    || this.currentUrl().startsWith('/change-password'),
  );

  constructor() {
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      takeUntilDestroyed(),
    ).subscribe(event => this.currentUrl.set(event.urlAfterRedirects));

    effect(onCleanup => {
      if (!this.session.token() || this.session.passwordChangeRequired() || this.session.user()) return;

      const subscription = this.api.me().subscribe({
        next: user => this.session.setUser(user),
        error: () => this.session.setUser(null),
      });
      onCleanup(() => subscription.unsubscribe());
    });
  }
}
