import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Endpoint } from '../../endpoints/models/endpoint.models';
import { Integration } from '../../integrations/models/integration.models';

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
  templateUrl: './execution-filters.component.html',
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
