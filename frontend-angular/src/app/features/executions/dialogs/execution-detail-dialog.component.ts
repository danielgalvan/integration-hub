import { ChangeDetectionStrategy, Component, Inject, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Execution } from '../models/execution.models';
import { ExecutionsApiService } from '../services/executions-api.service';

@Component({
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  templateUrl: './execution-detail-dialog.component.html',
  styleUrl: './execution-detail-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionDetailDialogComponent {
  private readonly api = inject(ExecutionsApiService);
  readonly loading = signal(true);
  readonly execution = signal<Execution | null>(null);
  readonly error = signal('');

  constructor(@Inject(MAT_DIALOG_DATA) readonly summary: Execution) {
    this.api.execution(summary.id).subscribe({
      next: execution => { this.execution.set(execution); this.loading.set(false); },
      error: error => { this.error.set(error.status === 404 ? 'Execução não encontrada.' : error.error?.message || 'Não foi possível carregar os detalhes da execução.'); this.execution.set(summary); this.loading.set(false); },
    });
  }

  details(item: Execution): { label: string; value: string }[] {
    return [
      { label: 'ID', value: this.value(item.id) },
      { label: 'Integração', value: this.value(item.integrationName) },
      { label: 'ID da integração', value: this.value(item.integrationId) },
      { label: 'Endpoint', value: this.value(item.endpointName) },
      { label: 'ID do endpoint', value: this.value(item.endpointId) },
      { label: 'Método', value: this.value(item.requestMethod) },
      { label: 'Caminho', value: this.value(item.requestPath) },
      { label: 'Status', value: this.statusLabel(item.executionStatus) },
      { label: 'HTTP', value: this.value(item.httpStatus) },
      { label: 'Duração', value: item.durationMs == null ? '—' : `${item.durationMs} ms` },
      { label: 'Credencial', value: this.value(item.credentialName) },
      { label: 'Início', value: this.date(item.startedAt) },
      { label: 'Fim', value: this.date(item.finishedAt) },
      { label: 'Criado em', value: this.date(item.createdAt) },
      { label: 'Tipo de erro', value: this.value(item.errorType) },
      { label: 'Mensagem de erro', value: this.value(item.errorMessage) },
    ];
  }

  formatParameters(parameters: unknown): string {
    if (parameters == null || parameters === '') return '{}';
    if (typeof parameters !== 'string') return JSON.stringify(parameters, null, 2) ?? '{}';
    try { return JSON.stringify(JSON.parse(parameters) as unknown, null, 2); } catch { return parameters; }
  }

  private value(value: unknown): string { return value === null || value === undefined || value === '' ? '—' : String(value); }
  private date(value: string | undefined): string {
    if (!value) return '—';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat('pt-BR').format(date);
  }
  private statusLabel(status: string): string {
    return ({ SUCCESS: 'Sucesso', ERROR: 'Erro', UNAUTHORIZED: 'Não autorizado', NOT_FOUND: 'Não encontrado' })[status] || this.value(status);
  }
}
