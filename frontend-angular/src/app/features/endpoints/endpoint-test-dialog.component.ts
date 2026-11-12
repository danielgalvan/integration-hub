import { ChangeDetectionStrategy, Component, Inject, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../core/api.service';
import { Endpoint, EndpointExecutionResult, Integration } from '../../core/models';
import { parameterValueValidator } from './parameter-value.validators';

interface EndpointTestDialogData {
  integration: Integration;
  endpoint: Endpoint;
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressBarModule, MatSnackBarModule],
  template: `
    <h2 mat-dialog-title class="dialog-title"><span><small>TESTE DE EXECUÇÃO</small>{{ data.endpoint.name }}</span><button mat-icon-button type="button" mat-dialog-close aria-label="Fechar"><mat-icon>close</mat-icon></button></h2>
    <mat-dialog-content class="dialog-content">
      <p class="route-line"><span class="method">GET</span><code>{{ data.integration.basePath }}{{ data.endpoint.path }}</code></p>
      @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
      <form [formGroup]="parametersForm" (ngSubmit)="execute()">
        @if (data.integration.authType === 'API_KEY') {
          <section class="field-section"><h3>Autenticação</h3>
            <mat-form-field appearance="outline" class="full"><mat-label>API Key</mat-label><input matInput type="password" [formControl]="apiKey" placeholder="ihub_..." autocomplete="off" required><mat-hint>Enviada no header X-API-Key</mat-hint>@if (apiKey.hasError('required') && apiKey.touched) { <mat-error>Informe a API Key da integração.</mat-error> }</mat-form-field>
          </section>
        }
        <section class="field-section"><h3>Parâmetros</h3>
          @if (data.endpoint.parameters.length) {
            <div class="parameter-grid">
              @for (parameter of data.endpoint.parameters; track parameter.name) {
                <mat-form-field appearance="outline"><mat-label>{{ parameter.name }}{{ parameter.required ? ' *' : '' }}</mat-label><input matInput type="text" [formControlName]="parameter.name" [placeholder]="hint(parameter.type)" [attr.inputmode]="inputMode(parameter.type)"><mat-hint>{{ parameterHint(parameter.type) }}</mat-hint>@if (parametersForm.controls[parameter.name].hasError('required') && parametersForm.controls[parameter.name].touched) { <mat-error>Este parâmetro é obrigatório.</mat-error> } @else if (parametersForm.controls[parameter.name].invalid && parametersForm.controls[parameter.name].touched) { <mat-error>{{ parameterError(parameter.type) }}</mat-error> }</mat-form-field>
              }
            </div>
          } @else { <p class="no-parameters">Este endpoint não possui parâmetros.</p> }
          <button mat-flat-button color="primary" type="submit" [disabled]="loading() || parametersForm.invalid || (data.integration.authType === 'API_KEY' && apiKey.invalid)"><mat-icon>play_arrow</mat-icon>{{ loading() ? 'Executando…' : 'Executar endpoint' }}</button>
        </section>
      </form>
      @if (error()) { <div class="error-banner" role="alert"><mat-icon>error_outline</mat-icon>{{ error() }}</div> }
      @if (result(); as response) {
        <section class="result-card">
          <div class="result-heading"><h3>Resultado</h3><div class="result-meta"><span class="status" [class.failed]="!response.success">HTTP {{ response.status }} {{ response.statusText }}</span><span>{{ response.duration }} ms</span></div></div>
          <div class="url-block"><div><strong>URL executada</strong><button mat-stroked-button type="button" (click)="copyUrl()"><mat-icon>{{ copied() ? 'check' : 'content_copy' }}</mat-icon>{{ copied() ? 'Copiada' : 'Copiar URL' }}</button></div><code>{{ response.url }}</code></div>
          <pre class="response-body">{{ formatData(response.data) }}</pre>
        </section>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button type="button" mat-dialog-close>Fechar</button></mat-dialog-actions>
  `,
  styles: [`
    .dialog-title{display:flex;align-items:center;justify-content:space-between;gap:12px;padding-right:8px}.dialog-title span{display:grid;gap:4px}.dialog-title small{color:var(--ihub-muted);font-size:.6rem;letter-spacing:.13em}.dialog-content{min-height:160px}.route-line{display:flex;align-items:center;gap:9px;margin:0 0 16px;color:var(--ihub-text-secondary)}.route-line code{overflow-wrap:anywhere}.method{padding:4px 7px;border-radius:5px;background:var(--ihub-info-container);color:var(--ihub-info);font-size:.68rem;font-weight:800}.field-section{display:grid;gap:12px;margin:8px 0 20px}.field-section h3,.result-heading h3{margin:0;font-size:.95rem}.full{width:100%}.parameter-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:4px 14px}.parameter-grid mat-form-field{width:100%}.no-parameters{margin:0 0 8px;color:var(--ihub-muted);font-size:.84rem}.error-banner{display:flex;gap:9px;align-items:center;margin:14px 0;padding:11px;border-radius:9px;background:var(--ihub-danger-container);color:var(--ihub-danger);font-size:.84rem}.result-card{display:grid;gap:14px;margin:18px 0 6px;padding:16px;border:1px solid var(--ihub-outline);border-radius:12px;background:var(--ihub-surface-container)}.result-heading,.result-meta,.url-block>div{display:flex;align-items:center;justify-content:space-between;gap:10px}.result-meta{color:var(--ihub-text-secondary);font-size:.8rem}.status{padding:4px 8px;border-radius:99px;background:var(--ihub-success-container);color:var(--ihub-success);font-weight:700}.status.failed{background:var(--ihub-danger-container);color:var(--ihub-danger)}.url-block{display:grid;gap:8px}.url-block>div strong{font-size:.8rem}.url-block button{font-size:.74rem}.url-block code{padding:10px;border-radius:7px;background:var(--ihub-surface-subtle);overflow-wrap:anywhere;font-size:.76rem}.response-body{max-height:330px;overflow:auto;margin:0;padding:13px;border-radius:8px;background:#101828;color:#e4e7ec;white-space:pre-wrap;overflow-wrap:anywhere;font: .78rem/1.55 Consolas,'Courier New',monospace}@media(max-width:620px){.parameter-grid{grid-template-columns:1fr}.result-heading,.result-meta{align-items:flex-start;flex-direction:column}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EndpointTestDialogComponent {
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);
  readonly loading = signal(false);
  readonly result = signal<EndpointExecutionResult | null>(null);
  readonly error = signal('');
  readonly copied = signal(false);
  readonly apiKey = new FormControl('', { nonNullable: true, validators: [Validators.required] });
  readonly parametersForm: FormGroup<Record<string, FormControl<string>>>;

  constructor(@Inject(MAT_DIALOG_DATA) readonly data: EndpointTestDialogData) {
    const controls: Record<string, FormControl<string>> = {};
    data.endpoint.parameters.forEach(parameter => {
      controls[parameter.name] = new FormControl('', {
        nonNullable: true,
        validators: [
          ...(parameter.required ? [Validators.required] : []),
          ...(parameterValueValidator(parameter.type) ? [parameterValueValidator(parameter.type)!] : []),
        ],
      });
    });
    this.parametersForm = new FormGroup(controls);
    if (data.integration.authType !== 'API_KEY') this.apiKey.clearValidators();
    this.apiKey.updateValueAndValidity();
  }

  hint(type: string): string {
    switch (type.toUpperCase()) {
      case 'DATE': return 'aaaa-mm-dd';
      case 'TIMESTAMP': return "aaaa-mm-dd'T'hh:mm:ss";
      case 'NUMBER': return 'Digite um número';
      default: return 'Digite um valor';
    }
  }

  parameterHint(type: string): string {
    switch (type.toUpperCase()) {
      case 'DATE': return 'DATE — formato yyyy-MM-dd';
      case 'TIMESTAMP': return "TIMESTAMP — formato yyyy-MM-dd'T'HH:mm:ss";
      case 'NUMBER': return 'NUMBER — número decimal, sem separador de milhar';
      default: return type;
    }
  }

  parameterError(type: string): string {
    switch (type.toUpperCase()) {
      case 'DATE': return 'Use uma data válida no formato yyyy-MM-dd.';
      case 'TIMESTAMP': return "Use uma data e hora válidas no formato yyyy-MM-dd'T'HH:mm:ss.";
      case 'NUMBER': return 'Informe um número válido, usando ponto como separador decimal.';
      default: return 'Informe um valor válido.';
    }
  }

  inputMode(type: string): string | null { return type === 'NUMBER' || type === 'DATE' || type === 'TIMESTAMP' ? 'decimal' : null; }

  execute(): void {
    if (this.parametersForm.invalid || (this.data.integration.authType === 'API_KEY' && this.apiKey.invalid)) {
      this.parametersForm.markAllAsTouched();
      this.apiKey.markAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.result.set(null);
    this.copied.set(false);
    const started = performance.now();
    const values = this.parametersForm.getRawValue();
    const key = this.data.integration.authType === 'API_KEY' ? this.apiKey.value.trim() : '';
    const url = this.api.endpointUrl(this.data.integration, this.data.endpoint, values);

    this.api.execute(this.data.integration, this.data.endpoint, values, key).subscribe({
      next: response => {
        this.result.set({
          success: response.ok,
          status: response.status,
          statusText: response.statusText,
          url,
          duration: Math.round(performance.now() - started),
          data: this.parseBody(response.body, response.headers.get('content-type') || ''),
        });
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        if (error.status) {
          this.result.set({
            success: false,
            status: error.status,
            statusText: error.statusText,
            url: error.url || url,
            duration: Math.round(performance.now() - started),
            data: this.parseBody(error.error, error.headers.get('content-type') || ''),
          });
        } else {
          this.error.set('Não foi possível executar o endpoint. Verifique a conexão com o backend.');
        }
      },
    });
  }

  formatData(data: unknown): string {
    return typeof data === 'string' ? data : JSON.stringify(data, null, 2) ?? '';
  }

  async copyUrl(): Promise<void> {
    const url = this.result()?.url;
    if (!url) return;
    try {
      await navigator.clipboard.writeText(url);
      this.copied.set(true);
      this.snack.open('URL copiada.', 'Fechar', { duration: 1600 });
    } catch {
      this.error.set('Não foi possível copiar a URL. Selecione e copie o endereço manualmente.');
    }
  }

  private parseBody(body: unknown, contentType: string): unknown {
    if (typeof body !== 'string') return body;
    if (!contentType.includes('application/json')) return body;
    try { return JSON.parse(body) as unknown; } catch { return body; }
  }
}
