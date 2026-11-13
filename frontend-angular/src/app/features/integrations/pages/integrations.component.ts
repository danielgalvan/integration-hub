import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Integration } from '../models/integration.models';
import { SessionService } from '../../../core/session.service';
import { ConfirmationService } from '../../../shared/dialogs/confirmation.service';
import { SecretDialogComponent } from '../../../shared/dialogs/secret-dialog.component';
import { IntegrationsApiService } from '../services/integrations-api.service';
import { CredentialsDialogComponent } from '../dialogs/credentials-dialog.component';
import { IntegrationFormComponent } from '../components/integration-form.component';
import { IntegrationListComponent } from '../components/integration-list.component';

@Component({
  standalone: true,
  imports: [
    IntegrationFormComponent,
    IntegrationListComponent,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  templateUrl: './integrations.component.html',
  styleUrl: './integrations.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IntegrationsComponent {
  readonly api = inject(IntegrationsApiService);
  readonly session = inject(SessionService);
  private readonly dialog = inject(MatDialog);
  private readonly confirm = inject(ConfirmationService);
  private readonly snack = inject(MatSnackBar);
  private readonly router = inject(Router);
  readonly items = signal<Integration[]>([]);
  readonly selected = signal<Integration | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.integrations().subscribe({
      next: items => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: error => {
        this.items.set([]);
        this.error.set(error.error?.message || 'Não foi possível carregar as integrações.');
        this.loading.set(false);
      },
    });
  }

  newIntegration(): void {
    if (!this.session.canEdit()) return;
    this.error.set('');
    this.selected.set({ name: '', basePath: '', active: 'S', authType: 'NONE' });
  }

  editIntegration(integration: Integration): void {
    this.error.set('');
    this.selected.set(integration);
  }

  closeForm(): void {
    this.selected.set(null);
    this.error.set('');
  }

  save(integration: Integration): void {
    if (!this.session.canEdit() || this.saving()) return;
    this.saving.set(true);
    this.error.set('');
    const request = integration.id
      ? this.api.updateIntegration(integration.id, integration)
      : this.api.createIntegration(integration);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.closeForm();
        this.snack.open('Integração salva com sucesso.', 'Fechar', { duration: 2500 });
        this.load();
      },
      error: error => {
        this.saving.set(false);
        this.error.set(error.error?.message || 'Não foi possível salvar a integração.');
      },
    });
  }

  openEndpoints(integration: Integration): void {
    void this.router.navigate(['/integrations', integration.id, 'endpoints'], { state: { integration } });
  }

  manageCredentials(integration: Integration): void {
    if (!this.session.canEdit() || integration.authType !== 'API_KEY') return;
    this.dialog.open(CredentialsDialogComponent, {
      width: 'min(900px, 96vw)',
      maxWidth: '96vw',
      data: integration,
      autoFocus: 'dialog',
      restoreFocus: true,
    });
  }

  generateLegacyKey(integration: Integration): void {
    if (!this.session.canEdit()) return;
    const regenerating = Boolean(integration.apiKeyCreatedAt);
    this.confirm.confirm({
      title: regenerating ? 'Regenerar API Key legada?' : 'Gerar API Key legada?',
      message: regenerating
        ? `A chave atual da integração “${integration.name}” deixará de funcionar imediatamente.`
        : `Será criada uma API Key legada para a integração “${integration.name}”.`,
      confirmLabel: regenerating ? 'Regenerar chave' : 'Gerar chave',
      destructive: regenerating,
    }).subscribe(confirmed => {
      if (!confirmed || !integration.id) return;
      this.api.generateIntegrationApiKey(integration.id).subscribe({
        next: value => {
          this.dialog.open(SecretDialogComponent, {
            width: 'min(560px, 94vw)',
            data: {
              title: 'API Key gerada',
              description: 'Copie e armazene esta chave em um local seguro.',
              warning: 'Esta chave legada será exibida somente agora. As credenciais múltiplas são recomendadas para novos consumidores.',
              secret: value.apiKey,
              label: 'API Key',
            },
          });
          this.load();
        },
        error: error => this.error.set(error.error?.message || 'Não foi possível gerar a API Key.'),
      });
    });
  }

  removeIntegration(integration: Integration): void {
    if (!this.session.canEdit() || !integration.id) return;
    this.confirm.confirm({
      title: 'Excluir integração?',
      message: `A integração “${integration.name}” será removida. Essa ação não pode ser desfeita.`,
      confirmLabel: 'Excluir integração',
      destructive: true,
    }).subscribe(confirmed => {
      if (!confirmed) return;
      this.api.deleteIntegration(integration.id!).subscribe({
        next: () => {
          this.snack.open('Integração excluída.', 'Fechar', { duration: 2200 });
          this.load();
        },
        error: error => this.error.set(error.error?.message || 'Não foi possível excluir a integração.'),
      });
    });
  }
}
