import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ApiService } from '../../core/api.service';
import { Endpoint, Integration } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { ConfirmationService } from '../../shared/dialogs/confirmation.service';
import { EndpointFormComponent } from './components/endpoint-form.component';
import { EndpointListComponent } from './components/endpoint-list.component';
import { EndpointTestDialogComponent } from './endpoint-test-dialog.component';

@Component({
  standalone: true,
  imports: [EndpointFormComponent, EndpointListComponent, MatButtonModule, MatDialogModule, MatIconModule, MatProgressBarModule, MatSnackBarModule],
  template: `
    <section class="page endpoints-page">
      <header class="page-header"><div>
        <button mat-button type="button" (click)="back()"><mat-icon>arrow_back</mat-icon> Integrações</button>
        <p class="eyebrow">{{ integration()?.name || 'CATÁLOGO DE APIS' }}</p>
        <h2>{{ formOpen() ? formTitle() : 'Endpoints' }}</h2>
        <p class="description">{{ formOpen() ? formDescription() : 'Gerencie as rotas GET e as consultas de leitura publicadas por esta integração.' }}</p>
      </div>
        @if (!formOpen() && integration() && session.canEdit()) { <button mat-flat-button color="primary" type="button" (click)="newEndpoint()"><mat-icon>add</mat-icon>Novo endpoint</button> }
      </header>
      @if (error()) { <div class="error-banner" role="alert"><mat-icon>error_outline</mat-icon>{{ error() }}<button mat-button type="button" (click)="error.set('')">Fechar</button></div> }
      @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
      @if (!integration() && !loading()) {
        <section class="empty-card"><mat-icon>hub</mat-icon><h3>Integração não encontrada</h3><p>Volte ao catálogo e selecione uma integração.</p><button mat-button type="button" (click)="back()">Voltar para integrações</button></section>
      } @else if (integration() && formOpen()) {
        <ihub-endpoint-form
          [integrationId]="integration()!.id!"
          [endpoint]="editing()"
          [readOnly]="!session.canEdit()"
          [saving]="saving()"
          [error]="formError()"
          (saved)="save($event)"
          (validationError)="formError.set($event)"
          (cancel)="closeForm()"
        />
      } @else if (integration() && !loading()) {
        <ihub-endpoint-list
          [endpoints]="items()"
          [canEdit]="session.canEdit()"
          [loading]="loading()"
          (test)="test($event)"
          (edit)="edit($event)"
          (remove)="remove($event)"
        />
      }
    </section>
  `,
  styles: [`
    .endpoints-page{display:grid;gap:18px}.eyebrow{margin:4px 0;color:#667085;font-size:.68rem;font-weight:800;letter-spacing:.13em;text-transform:uppercase}.description{margin:6px 0 0;color:#667085}.error-banner{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:9px;background:#fef3f2;color:#b42318;font-size:.85rem}.error-banner button{margin-left:auto}.empty-card{display:grid;justify-items:center;gap:8px;padding:48px 20px;border:1px dashed #d0d5dd;border-radius:14px;background:#fff;text-align:center}.empty-card mat-icon{color:#98a2b3;font-size:40px}.empty-card h3,.empty-card p{margin:0}.empty-card p{color:#667085}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EndpointsComponent {
  readonly api = inject(ApiService);
  readonly session = inject(SessionService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly confirmation = inject(ConfirmationService);
  private readonly snack = inject(MatSnackBar);

  readonly integration = signal<Integration | null>(history.state.integration || null);
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
    const request = current?.id ? this.api.updateEndpoint(current.id, endpoint) : this.api.createEndpoint(endpoint);
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
      this.api.deleteEndpoint(endpoint.id!).subscribe({
        next: () => { this.snack.open('Endpoint excluído.', 'Fechar', { duration: 1800 }); this.loadEndpoints(); },
        error: error => this.error.set(error.error?.message || 'Não foi possível excluir o endpoint.'),
      });
    });
  }

  back(): void { void this.router.navigateByUrl('/integrations'); }

  private loadIntegration(id: number): void {
    this.loading.set(true);
    this.api.integrations().subscribe({
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
    this.api.endpoints(id).subscribe({
      next: endpoints => { this.items.set(endpoints); this.loading.set(false); },
      error: error => { this.error.set(error.error?.message || 'Não foi possível carregar os endpoints.'); this.loading.set(false); },
    });
  }
}
