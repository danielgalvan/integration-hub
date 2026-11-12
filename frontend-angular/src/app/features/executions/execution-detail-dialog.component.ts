import { ChangeDetectionStrategy, Component, Inject, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Execution } from '../../core/models';
import { ExecutionsApiService } from './executions-api.service';

@Component({
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  template: `
    <h2 mat-dialog-title class="dialog-title"><span><small>HISTÓRICO DE EXECUÇÃO</small>Detalhes da execução #{{ execution()?.id || summary.id }}</span><button mat-icon-button type="button" mat-dialog-close aria-label="Fechar"><mat-icon>close</mat-icon></button></h2>
    <mat-dialog-content>
      @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
      @if (error()) { <div class="error-banner" role="alert"><mat-icon>error_outline</mat-icon>{{ error() }}</div> }
      @if (execution(); as item) {
        <div class="details-grid">
          @for (field of details(item); track field.label) { <div class="detail"><span>{{ field.label }}</span><strong [class.error-text]="field.label === 'Mensagem de erro' && field.value !== '—'">{{ field.value }}</strong></div> }
        </div>
        <section class="parameters"><h3>Parâmetros da requisição</h3><pre>{{ formatParameters(item.requestParameters) }}</pre></section>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button type="button" mat-dialog-close>Fechar</button></mat-dialog-actions>
  `,
  styles: [`
    .dialog-title{display:flex;align-items:center;justify-content:space-between;gap:12px;padding-right:8px}.dialog-title span{display:grid;gap:4px}.dialog-title small{color:var(--ihub-muted);font-size:.6rem;letter-spacing:.13em}.error-banner{display:flex;align-items:center;gap:8px;margin:12px 0;padding:10px;border-radius:8px;background:var(--ihub-danger-container);color:var(--ihub-danger);font-size:.82rem}.details-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:1px 18px}.detail{display:grid;gap:4px;padding:12px 0;border-bottom:1px solid var(--ihub-border);min-width:0}.detail span{color:var(--ihub-muted);font-size:.72rem}.detail strong{overflow-wrap:anywhere;font-size:.84rem;font-weight:600}.error-text{color:var(--ihub-danger)}.parameters{margin-top:18px}.parameters h3{margin:0 0 8px;font-size:.9rem}.parameters pre{max-height:240px;overflow:auto;margin:0;padding:12px;border-radius:8px;background:#101828;color:#e4e7ec;white-space:pre-wrap;overflow-wrap:anywhere;font: .77rem/1.55 Consolas,'Courier New',monospace}@media(max-width:600px){.details-grid{grid-template-columns:1fr}}
  `],
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
