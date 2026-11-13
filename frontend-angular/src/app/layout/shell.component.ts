import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { BreakpointObserver } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SessionService } from '../core/auth/session.service';
import { ThemeService } from '../core/services/theme.service';

@Component({
  selector: 'ihub-shell',
  standalone: true,
  styleUrl: './shell.component.scss',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
    RouterLink,
    RouterLinkActive,
  ],
  template: `
    <mat-sidenav-container class="shell-container">
      <mat-sidenav
        #drawer
        class="sidebar"
        [mode]="mobile() ? 'over' : 'side'"
        [opened]="!mobile() || menuOpen()"
        (closedStart)="menuOpen.set(false)"
      >
        <a class="brand" routerLink="/integrations" (click)="closeMobileMenu()">
          <span class="brand-mark">IH</span>
          <span class="brand-copy">
            <strong>Integration Hub</strong>
            <small>Administração de APIs</small>
          </span>
        </a>

        <mat-nav-list aria-label="Navegação principal">
          <a mat-list-item routerLink="/integrations" routerLinkActive="active-link" (click)="closeMobileMenu()">
            <mat-icon matListItemIcon>hub</mat-icon>
            <span matListItemTitle>Integrações</span>
          </a>
          @if (session.isAdmin()) {
            <a mat-list-item routerLink="/executions" routerLinkActive="active-link" (click)="closeMobileMenu()">
              <mat-icon matListItemIcon>history</mat-icon>
              <span matListItemTitle>Histórico de execuções</span>
            </a>
            <a mat-list-item routerLink="/users" routerLinkActive="active-link" (click)="closeMobileMenu()">
              <mat-icon matListItemIcon>manage_accounts</mat-icon>
              <span matListItemTitle>Usuários</span>
            </a>
          }
        </mat-nav-list>

        <div class="sidebar-footer">
          <span class="environment-indicator"></span>
          <span class="environment-copy">
            <small>AMBIENTE</small>
            <strong>{{ session.environmentName() || 'Não selecionado' }}</strong>
          </span>
        </div>
      </mat-sidenav>

      <mat-sidenav-content class="workspace">
        <mat-toolbar class="topbar">
          @if (mobile()) {
            <button mat-icon-button aria-label="Abrir navegação" (click)="menuOpen.update(open => !open)">
              <mat-icon>menu</mat-icon>
            </button>
          }
          <div class="page-heading">
            <small>Integration Hub</small>
            <h1>{{ pageTitle() }}</h1>
          </div>
          <span class="toolbar-spacer"></span>
          <button mat-icon-button [matTooltip]="theme.dark() ? 'Usar tema claro' : 'Usar tema escuro'" [attr.aria-label]="theme.dark() ? 'Usar tema claro' : 'Usar tema escuro'" (click)="theme.toggle()">
            <mat-icon>{{ theme.dark() ? 'light_mode' : 'dark_mode' }}</mat-icon>
          </button>
          <div class="user-summary">
            <span class="avatar">{{ initials() }}</span>
            <span class="user-copy">
              <strong>{{ session.user()?.name || session.user()?.username || 'Usuário' }}</strong>
              <small>{{ roleLabel() }}</small>
            </span>
          </div>
          <button mat-stroked-button class="logout-button" type="button" (click)="logout()">
            Sair
          </button>
        </mat-toolbar>

        <main class="page-content"><ng-content /></main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShellComponent {
  readonly session = inject(SessionService);
  readonly theme = inject(ThemeService);
  private readonly router = inject(Router);
  private readonly breakpoints = inject(BreakpointObserver);
  readonly menuOpen = signal(false);
  readonly mobile = toSignal(
    this.breakpoints.observe('(max-width: 800px)').pipe(map(result => result.matches)),
    { initialValue: false },
  );
  private readonly route = toSignal(this.router.events.pipe(
    filter((event): event is NavigationEnd => event instanceof NavigationEnd),
    map(event => event.urlAfterRedirects),
  ), { initialValue: this.router.url });

  readonly roleLabel = computed(() => ({ A: 'Administrador', C: 'Criador', U: 'Consumidor' })[this.session.role() || 'U']);
  readonly pageTitle = computed(() => {
    const route = this.route();
    if (route.includes('/endpoints')) return 'Endpoints';
    if (route.startsWith('/executions')) return 'Histórico de execuções';
    if (route.startsWith('/users')) return 'Usuários';
    return 'Integrações';
  });
  readonly initials = computed(() => {
    const name = this.session.user()?.name || this.session.user()?.username || 'U';
    return name.trim().split(/\s+/).slice(0, 2).map(part => part[0]?.toUpperCase()).join('');
  });

  closeMobileMenu(): void {
    if (this.mobile()) this.menuOpen.set(false);
  }

  logout(): void {
    this.session.clear();
    void this.router.navigateByUrl('/login');
  }
}
