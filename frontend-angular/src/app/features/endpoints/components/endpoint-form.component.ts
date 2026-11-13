import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Endpoint, EndpointParameter, ParameterType } from '../models/endpoint.models';

type ParameterControls = {
  name: FormControl<string>;
  type: FormControl<ParameterType>;
  required: FormControl<boolean>;
};

@Component({
  selector: 'ihub-endpoint-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, MatSlideToggleModule],
  templateUrl: './endpoint-form.component.html',
  styleUrl: './endpoint-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EndpointFormComponent implements OnChanges {
  @Input() integrationId = 0;
  @Input() endpoint: Endpoint | null = null;
  @Input() readOnly = false;
  @Input() saving = false;
  @Input() error = '';
  @Output() saved = new EventEmitter<Endpoint>();
  @Output() cancel = new EventEmitter<void>();
  @Output() validationError = new EventEmitter<string>();

  private readonly fb = inject(FormBuilder);
  readonly parameters = this.fb.array<FormGroup<ParameterControls>>([]);
  readonly form = this.fb.group({
    name: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(100)]),
    description: this.fb.nonNullable.control('', Validators.maxLength(500)),
    path: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200), Validators.pattern(/^\//)]),
    sqlText: this.fb.nonNullable.control('', Validators.required),
    active: this.fb.nonNullable.control(true),
    parameters: this.parameters,
  });

  constructor() {
    this.form.controls.sqlText.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.validationError.emit(''));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['endpoint'] || changes['readOnly']) this.resetForm();
    if (changes['readOnly']) this.toggleDisabledState();
  }

  generateParameters(): void {
    const existing = new Map(this.parameters.getRawValue().map(parameter => [parameter.name, parameter]));
    const names = this.extractParameterNames(this.form.controls.sqlText.value);
    this.parameters.clear();
    names.forEach(name => {
      const saved = existing.get(name);
      this.parameters.push(this.createParameter({
        name,
        type: saved?.type || 'VARCHAR2',
        required: saved?.required || false,
      }));
    });
    this.form.controls.sqlText.updateValueAndValidity();
    this.validationError.emit('');
  }

  submit(): void {
    if (this.readOnly || this.saving || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.parametersMatchSql()) {
      this.form.controls.sqlText.setErrors({ parametersOutOfSync: true });
      this.validationError.emit('O SQL foi alterado. Gere novamente os parâmetros antes de salvar.');
      return;
    }
    this.validationError.emit('');
    const value = this.form.getRawValue();
    this.saved.emit({
      id: this.endpoint?.id,
      integrationId: this.integrationId,
      name: value.name.trim(),
      description: value.description.trim(),
      path: value.path.trim(),
      method: 'GET',
      sqlText: value.sqlText.trim(),
      active: value.active ? 'S' : 'N',
      parameters: this.parameters.getRawValue(),
    });
  }

  private resetForm(): void {
    this.form.reset({
      name: this.endpoint?.name || '',
      description: this.endpoint?.description || '',
      path: this.endpoint?.path || '',
      sqlText: this.endpoint?.sqlText || '',
      active: this.endpoint?.active !== 'N',
    });
    this.parameters.clear();
    (this.endpoint?.parameters || []).forEach(parameter => this.parameters.push(this.createParameter(parameter)));
    this.toggleDisabledState();
  }

  private toggleDisabledState(): void {
    if (this.readOnly) this.form.disable({ emitEvent: false });
    else this.form.enable({ emitEvent: false });
  }

  private extractParameterNames(sql: string): string[] {
    return [...new Set(Array.from(sql.matchAll(/:([A-Za-z][A-Za-z0-9_]*)/g), match => match[1]))]
      .filter(name => name !== '__ih_max_results');
  }

  private parametersMatchSql(): boolean {
    const expected = this.extractParameterNames(this.form.controls.sqlText.value);
    const configured = this.parameters.getRawValue().map(parameter => parameter.name);
    return expected.length === configured.length && expected.every(name => configured.includes(name));
  }

  private createParameter(parameter: EndpointParameter): FormGroup<ParameterControls> {
    return this.fb.nonNullable.group({
      name: [parameter.name],
      type: [parameter.type],
      required: [parameter.required],
    });
  }
}
