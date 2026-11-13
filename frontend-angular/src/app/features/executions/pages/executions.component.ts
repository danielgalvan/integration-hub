import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { catchError, map, of, Subject, switchMap } from 'rxjs';
import { Endpoint } from '../../endpoints/models/endpoint.models';
import { Execution } from '../models/execution.models';
import { Integration } from '../../integrations/models/integration.models';
import { Page } from '../../../core/models/common.models';
import { EndpointsApiService } from '../../endpoints/services/endpoints-api.service';
import { IntegrationsApiService } from '../../integrations/services/integrations-api.service';
import { ExecutionsApiService } from '../services/executions-api.service';
import { ExecutionFilters, ExecutionFiltersComponent } from '../components/execution-filters.component';
import { ExecutionListComponent } from '../components/execution-list.component';
import { ExecutionDetailDialogComponent } from '../dialogs/execution-detail-dialog.component';

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
  templateUrl: './executions.component.html',
  styles: [`
    .executions-page{display:grid;gap:18px}.eyebrow{margin:4px 0;color:var(--ihub-muted);font-size:.68rem;font-weight:800;letter-spacing:.13em}.description{margin:6px 0 0;color:var(--ihub-muted)}.error-banner{display:flex;align-items:center;gap:10px;padding:11px 14px;border-radius:9px;background:var(--ihub-danger-container);color:var(--ihub-danger);font-size:.85rem}.error-banner button{margin-left:auto}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionsComponent {
  private readonly executionsApi = inject(ExecutionsApiService);
  private readonly endpointsApi = inject(EndpointsApiService);
  private readonly integrationsApi = inject(IntegrationsApiService);
  private readonly dialog = inject(MatDialog);
  private readonly pageRequests = new Subject<{ page: number; size: number; filters: ExecutionFilters }>();
  private readonly endpointRequests = new Subject<string>();
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
    this.pageRequests.pipe(
      switchMap(request => this.executionsApi.executions({ ...request.filters, page: request.page, size: request.size }).pipe(
        map(result => ({ result, request })),
        catchError(error => of({ error, request })),
      )),
      takeUntilDestroyed(),
    ).subscribe(response => {
      if ('result' in response) {
        this.page.set(response.result);
        this.loading.set(false);
      } else {
        this.error.set(response.error.error?.message || 'Não foi possível carregar o histórico de execuções.');
        this.page.set({ ...EMPTY_PAGE, page: response.request.page, size: response.request.size });
        this.loading.set(false);
      }
    });

    this.endpointRequests.pipe(
      switchMap(integrationId => integrationId
        ? this.endpointsApi.endpoints(Number(integrationId)).pipe(
          map(endpoints => ({ endpoints })),
          catchError(error => of({ error })),
        )
        : of({ endpoints: [] as Endpoint[] }),
      ),
      takeUntilDestroyed(),
    ).subscribe(response => {
      if ('endpoints' in response) {
        this.endpoints.set(response.endpoints);
      } else {
        this.error.set(response.error.error?.message || 'Não foi possível carregar os endpoints deste filtro.');
      }
      this.loadingEndpoints.set(false);
    });

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
    this.loadEndpoints('');
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
    this.integrationsApi.integrations().pipe(takeUntilDestroyed()).subscribe({
      next: integrations => { this.integrations.set(integrations); this.integrationsLoading.set(false); },
      error: error => { this.error.set(error.error?.message || 'Não foi possível carregar as integrações dos filtros.'); this.integrationsLoading.set(false); },
    });
  }

  private loadEndpoints(integrationId: string): void {
    this.endpoints.set([]);
    this.loadingEndpoints.set(Boolean(integrationId));
    this.endpointRequests.next(integrationId);
  }

  private loadPage(page: number, size: number): void {
    this.loading.set(true);
    this.error.set('');
    this.pageRequests.next({ page, size, filters: { ...this.appliedFilters() } });
  }
}
