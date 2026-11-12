import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Endpoint, Integration } from '../../../core/models';

export interface ExecutionFilters {
  integrationId: string;
  endpointId: string;
  executionStatus: string;
  startedFrom: string;
  startedTo: string;
}

@Component({
  selector: 'ihub-execution-filters',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule],
  template: `
    <mat-card class="filter-card"><form [formGroup]="form" (ngSubmit)="search.emit()">
      <mat-form-field appearance="outline"><mat-label>Integração</mat-label><mat-select formControlName="integrationId" (selectionChange)="integrationChanged()"><mat-option value="">Todas</mat-option>@for (integration of integrations; track integration.id) { <mat-option [value]="String(integration.id)">{{ integration.name }}</mat-option> }</mat-select></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Endpoint</mat-label><mat-select formControlName="endpointId" [disabled]="!form.controls.integrationId.value || loadingEndpoints" (selectionChange)="emitFilters()"><mat-option value="">Todos</mat-option>@for (endpoint of endpoints; track endpoint.id) { <mat-option [value]="String(endpoint.id)">{{ endpoint.name }}</mat-option> }</mat-select>@if (loadingEndpoints) { <mat-hint>Carregando endpoints…</mat-hint> }</mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Status</mat-label><mat-select formControlName="executionStatus" (selectionChange)="emitFilters()"><mat-option value="">Todos</mat-option><mat-option value="SUCCESS">Sucesso</mat-option><mat-option value="ERROR">Erro</mat-option><mat-option value="UNAUTHORIZED">Não autorizado</mat-option><mat-option value="NOT_FOUND">Não encontrado</mat-option></mat-select></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Data inicial</mat-label><input matInput type="datetime-local" formControlName="startedFrom" (input)="emitFilters()"></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Data final</mat-label><input matInput type="datetime-local" formControlName="startedTo" (input)="emitFilters()"></mat-form-field>
      <div class="actions"><button mat-flat-button color="primary" type="submit" [disabled]="loading"><mat-icon>search</mat-icon>Filtrar</button><button mat-button type="button" [disabled]="loading" (click)="clear.emit()"><mat-icon>filter_alt_off</mat-icon>Limpar</button></div>
    </form></mat-card>
  `,
  styles: [`
    .filter-card form{display:grid;grid-template-columns:repeat(3,minmax(160px,1fr));gap:4px 12px;align-items:center;padding-top:16px}.filter-card mat-form-field{width:100%}.actions{display:flex;align-items:center;gap:8px;flex-wrap:wrap;padding-bottom:20px}@media(max-width:900px){.filter-card form{grid-template-columns:repeat(2,minmax(140px,1fr))}}@media(max-width:580px){.filter-card form{grid-template-columns:1fr}.actions{padding-bottom:4px}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionFiltersComponent implements OnChanges {
  @Input() filters: ExecutionFilters = { integrationId: '', endpointId: '', executionStatus: '', startedFrom: '', startedTo: '' };
  @Input() integrations: Integration[] = [];
  @Input() endpoints: Endpoint[] = [];
  @Input() loadingEndpoints = false;
  @Input() loading = false;
  @Output() filtersChange = new EventEmitter<ExecutionFilters>();
  @Output() search = new EventEmitter<void>();
  @Output() clear = new EventEmitter<void>();
  private readonly fb = inject(FormBuilder);
  readonly form = this.fb.nonNullable.group({ integrationId: [''], endpointId: [''], executionStatus: [''], startedFrom: [''], startedTo: [''] });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['filters']) this.form.reset(this.filters, { emitEvent: false });
  }

  String(value: number | undefined): string { return value === undefined ? '' : String(value); }

  integrationChanged(): void {
    this.form.controls.endpointId.setValue('', { emitEvent: false });
    this.emitFilters();
  }

  emitFilters(): void { this.filtersChange.emit(this.form.getRawValue()); }
}
