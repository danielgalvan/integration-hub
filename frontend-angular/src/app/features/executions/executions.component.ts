import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ApiService } from '../../core/api.service';
import { Endpoint, Execution, Integration, Page } from '../../core/models';
import { ExecutionFilters, ExecutionFiltersComponent } from './components/execution-filters.component';
import { ExecutionListComponent } from './components/execution-list.component';
import { ExecutionDetailDialogComponent } from './execution-detail-dialog.component';

const INITIAL_FILTERS: ExecutionFilters = {
  integrationId: '',
  endpointId: '',
  executionStatus: '',
  startedFrom: '',
  startedTo: '',
};

const EMPTY_PAGE: Page<Execution> = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
};

@Component({
  standalone: true,
  imports: [ExecutionFiltersComponent, ExecutionListComponent, MatButtonModule, MatDialogModule, MatIconModule, MatProgressBarModule],
  template: `
    <section class="page executions-page">
      <header class="page-header"><div><p class="eyebrow">AUDITORIA</p><h2>Histórico de execuções</h2><p class="description">Consulte chamadas, resultados, credenciais e tempos registrados pelas integrações.</p></div>
        <button mat-stroked-button type="button" [disabled]="loading()" (click)="refresh()"><mat-icon>refresh</mat-icon>Atualizar</button>
      </header>
      @if (error()) { <div class="error-banner" role="alert"><mat-icon>error_outline</mat-icon>{{ error() }}<button mat-button type="button" (click)="error.set('')">Fechar</button></div> }
      @if (integrationsLoading()) { <mat-progress-bar mode="indeterminate" /> }
      <ihub-execution-filters
        [filters]="filters()"
        [integrations]="integrations()"
        [endpoints]="endpoints()"
        [loadingEndpoints]="loadingEndpoints()"
        [loading]="loading()"
        (filtersChange)="updateFilters($event)"
        (search)="search()"
        (clear)="clearFilters()"
      />
      <ihub-execution-list [page]="page()" [loading]="loading()" (details)="showDetails($event)" (pageChange)="changePage($event.pageIndex, $event.pageSize)" />
    </section>
  `,
  styles: [`
    .executions-page{display:grid;gap:18px}.eyebrow{margin:4px 0;color:#667085;font-size:.68rem;font-weight:800;letter-spacing:.13em}.description{margin:6px 0 0;color:#667085}.error-banner{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:9px;background:#fef3f2;color:#b42318;font-size:.85rem}.error-banner button{margin-left:auto}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionsComponent {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  readonly integrations = signal<Integration[]>([]);
  readonly endpoints = signal<Endpoint[]>([]);
  readonly filters = signal<ExecutionFilters>({ ...INITIAL_FILTERS });
  readonly appliedFilters = signal<ExecutionFilters>({ ...INITIAL_FILTERS });
  readonly page = signal<Page<Execution>>({ ...EMPTY_PAGE });
  readonly integrationsLoading = signal(false);
  readonly loadingEndpoints = signal(false);
  readonly loading = signal(false);
  readonly error = signal('');

  constructor() {
    this.loadIntegrations();
    this.loadPage(0, EMPTY_PAGE.size);
  }

  updateFilters(filters: ExecutionFilters): void {
    const previousIntegration = this.filters().integrationId;
    this.filters.set(filters);
    if (filters.integrationId !== previousIntegration) this.loadEndpoints(filters.integrationId);
  }

  search(): void {
    this.appliedFilters.set({ ...this.filters() });
    this.loadPage(0, this.page().size);
  }

  clearFilters(): void {
    this.filters.set({ ...INITIAL_FILTERS });
    this.appliedFilters.set({ ...INITIAL_FILTERS });
    this.endpoints.set([]);
    this.loadingEndpoints.set(false);
    this.loadPage(0, this.page().size);
  }

  refresh(): void {
    this.loadPage(this.page().page, this.page().size);
  }

  changePage(page: number, size: number): void {
    this.loadPage(page, size);
  }

  showDetails(execution: Execution): void {
    this.dialog.open(ExecutionDetailDialogComponent, {
      width: 'min(760px, 96vw)',
      maxWidth: '96vw',
      data: execution,
      autoFocus: 'dialog',
      restoreFocus: true,
    });
  }

  private loadIntegrations(): void {
    this.integrationsLoading.set(true);
    this.api.integrations().subscribe({
      next: integrations => { this.integrations.set(integrations); this.integrationsLoading.set(false); },
      error: error => { this.error.set(error.error?.message || 'Não foi possível carregar as integrações dos filtros.'); this.integrationsLoading.set(false); },
    });
  }

  private loadEndpoints(integrationId: string): void {
    this.endpoints.set([]);
    if (!integrationId) { this.loadingEndpoints.set(false); return; }
    this.loadingEndpoints.set(true);
    this.api.endpoints(Number(integrationId)).subscribe({
      next: endpoints => { this.endpoints.set(endpoints); this.loadingEndpoints.set(false); },
      error: error => { this.error.set(error.error?.message || 'Não foi possível carregar os endpoints deste filtro.'); this.loadingEndpoints.set(false); },
    });
  }

  private loadPage(page: number, size: number): void {
    this.loading.set(true);
    this.error.set('');
    this.api.executions({ ...this.appliedFilters(), page, size }).subscribe({
      next: result => { this.page.set(result); this.loading.set(false); },
      error: error => { this.error.set(error.error?.message || 'Não foi possível carregar o histórico de execuções.'); this.page.set({ ...EMPTY_PAGE, page, size }); this.loading.set(false); },
    });
  }
}
