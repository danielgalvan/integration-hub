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
import { Endpoint, EndpointExecutionResult } from '../models/endpoint.models';
import { Integration } from '../../integrations/models/integration.models';
import { EndpointsApiService } from '../services/endpoints-api.service';
import { parameterValueValidator } from '../parameter-value.validators';

interface EndpointTestDialogData {
  integration: Integration;
  endpoint: Endpoint;
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressBarModule, MatSnackBarModule],
  templateUrl: './endpoint-test-dialog.component.html',
  styleUrl: './endpoint-test-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EndpointTestDialogComponent {
  private readonly api = inject(EndpointsApiService);
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
