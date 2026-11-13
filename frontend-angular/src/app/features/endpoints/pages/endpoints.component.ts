import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Endpoint } from '../models/endpoint.models';
import { Integration } from '../../integrations/models/integration.models';
import { SessionService } from '../../../core/session.service';
import { ConfirmationService } from '../../../shared/dialogs/confirmation.service';
import { EndpointFormComponent } from '../components/endpoint-form.component';
import { EndpointListComponent } from '../components/endpoint-list.component';
import { EndpointTestDialogComponent } from '../dialogs/endpoint-test-dialog.component';
import { EndpointsApiService } from '../services/endpoints-api.service';
import { IntegrationsApiService } from '../../integrations/services/integrations-api.service';

@Component({
  standalone: true,
  imports: [EndpointFormComponent, EndpointListComponent, MatButtonModule, MatDialogModule, MatIconModule, MatProgressBarModule, MatSnackBarModule],
  templateUrl: './endpoints.component.html',
  styleUrl: './endpoints.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EndpointsComponent {
  readonly endpointsApi = inject(EndpointsApiService);
  readonly integrationsApi = inject(IntegrationsApiService);
  readonly session = inject(SessionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly confirmation = inject(ConfirmationService);
  private readonly snack = inject(MatSnackBar);

  readonly integration = signal<Integration | null>(history.state?.integration || null);
  readonly items = signal<Endpoint[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly formOpen = signal(false);
  readonly editing = signal<Endpoint | null>(null);
  readonly error = signal('');
  readonly formError = signal('');

  constructor() {
    const integrationId = Number(this.route.snapshot.paramMap.get('integrationId'));
    if (this.integration()?.id === integrationId) this.loadEndpoints();
    else this.loadIntegration(integrationId);
  }

  formTitle(): string {
    if (!this.session.canEdit()) return 'Visualizar endpoint';
    return this.editing()?.id ? 'Editar endpoint' : 'Novo endpoint';
  }

  formDescription(): string {
    const name = this.integration()?.name || 'integração';
    if (!this.session.canEdit()) return `Consulte os dados do endpoint da integração ${name}.`;
    return this.editing()?.id ? `Edite o endpoint da integração ${name}.` : `Cadastre um endpoint para a integração ${name}.`;
  }

  newEndpoint(): void {
    if (!this.session.canEdit()) return;
    this.editing.set(null);
    this.formError.set('');
    this.formOpen.set(true);
  }

  edit(endpoint: Endpoint): void {
    this.editing.set(endpoint);
    this.formError.set('');
    this.formOpen.set(true);
  }

  closeForm(): void {
    this.formOpen.set(false);
    this.editing.set(null);
    this.formError.set('');
  }

  save(endpoint: Endpoint): void {
    if (!this.session.canEdit() || this.saving()) return;
    this.saving.set(true);
    this.formError.set('');
    const current = this.editing();
    const request = current?.id
      ? this.endpointsApi.updateEndpoint(current.id, endpoint)
      : this.endpointsApi.createEndpoint(endpoint);
    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.closeForm();
        this.snack.open('Endpoint salvo com sucesso.', 'Fechar', { duration: 2200 });
        this.loadEndpoints();
      },
      error: error => {
        this.saving.set(false);
        this.formError.set(error.error?.message || 'Não foi possível salvar o endpoint.');
      },
    });
  }

  test(endpoint: Endpoint): void {
    this.dialog.open(EndpointTestDialogComponent, {
      width: 'min(820px, 96vw)',
      maxWidth: '96vw',
      data: { integration: this.integration(), endpoint },
      autoFocus: 'dialog',
      restoreFocus: true,
    });
  }

  remove(endpoint: Endpoint): void {
    if (!this.session.canEdit() || !endpoint.id) return;
    this.confirmation.confirm({
      title: 'Excluir endpoint?',
      message: `O endpoint “${endpoint.name}” será removido permanentemente. Essa ação não pode ser desfeita.`,
      confirmLabel: 'Excluir endpoint',
      destructive: true,
    }).subscribe(confirmed => {
      if (!confirmed) return;
      this.endpointsApi.deleteEndpoint(endpoint.id!).subscribe({
        next: () => { this.snack.open('Endpoint excluído.', 'Fechar', { duration: 1800 }); this.loadEndpoints(); },
        error: error => this.error.set(error.error?.message || 'Não foi possível excluir o endpoint.'),
      });
    });
  }

  back(): void { void this.router.navigateByUrl('/integrations'); }

  private loadIntegration(id: number): void {
    this.loading.set(true);
    this.integrationsApi.integrations().subscribe({
      next: integrations => {
        this.integration.set(integrations.find(integration => integration.id === id) || null);
        if (this.integration()) this.loadEndpoints();
        else this.loading.set(false);
      },
      error: error => { this.error.set(error.error?.message || 'Não foi possível carregar a integração.'); this.loading.set(false); },
    });
  }

  private loadEndpoints(): void {
    const id = this.integration()?.id;
    if (!id) { this.loading.set(false); return; }
    this.loading.set(true);
    this.endpointsApi.endpoints(id).subscribe({
      next: endpoints => { this.items.set(endpoints); this.loading.set(false); },
      error: error => { this.error.set(error.error?.message || 'Não foi possível carregar os endpoints.'); this.loading.set(false); },
    });
  }
}
