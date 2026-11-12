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
import { Endpoint, EndpointParameter, ParameterType } from '../../../core/models';

type ParameterControls = {
  name: FormControl<string>;
  type: FormControl<ParameterType>;
  required: FormControl<boolean>;
};

@Component({
  selector: 'ihub-endpoint-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, MatSlideToggleModule],
  template: `
    <mat-card class="editor-card">
      <mat-card-header>
        <mat-card-title>{{ readOnly ? 'Visualizar endpoint' : endpoint?.id ? 'Editar endpoint' : 'Novo endpoint' }}</mat-card-title>
        <mat-card-subtitle>{{ readOnly ? 'SQL, parâmetros e status deste endpoint.' : 'Configure a rota GET e a consulta SQL parametrizada.' }}</mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-grid">
            <mat-form-field appearance="outline">
              <mat-label>Nome</mat-label>
              <input matInput formControlName="name" maxlength="100" placeholder="Buscar pedidos">
              @if (form.controls.name.hasError('required')) { <mat-error>Informe o nome do endpoint.</mat-error> }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Caminho</mat-label>
              <input matInput formControlName="path" maxlength="200" placeholder="/buscar">
              @if (form.controls.path.hasError('required')) { <mat-error>Informe o caminho.</mat-error> }
              @if (form.controls.path.hasError('pattern')) { <mat-error>O caminho deve começar com /.</mat-error> }
            </mat-form-field>
            <mat-form-field appearance="outline" class="wide">
              <mat-label>Descrição</mat-label>
              <textarea matInput formControlName="description" rows="2" maxlength="500"></textarea>
            </mat-form-field>
            <mat-form-field appearance="outline" class="wide sql-field">
              <mat-label>SQL de leitura</mat-label>
              <textarea matInput formControlName="sqlText" rows="8" placeholder="select id, nome from cliente where id = :id"></textarea>
              @if (form.controls.sqlText.hasError('required')) { <mat-error>Informe a consulta SQL.</mat-error> }
            </mat-form-field>
            <div class="toggle-cell"><mat-slide-toggle formControlName="active">Endpoint ativo</mat-slide-toggle><span class="method-chip">GET</span></div>
          </div>

          <section class="parameters-section">
            <div class="parameters-heading">
              <div><h3>Parâmetros</h3><p>{{ readOnly ? 'Parâmetros associados às bind variables do SQL.' : 'Gere a lista a partir das bind variables e configure o tipo de cada parâmetro.' }}</p></div>
              @if (!readOnly) { <button mat-stroked-button type="button" (click)="generateParameters()"><mat-icon>auto_awesome</mat-icon>Gerar pelo SQL</button> }
            </div>
            @if (!parameters.length) { <div class="parameter-empty">Este endpoint não possui parâmetros configurados.</div> }
            <div class="parameter-list" formArrayName="parameters">
              @for (parameter of parameters.controls; track parameter.controls.name.value; let index = $index) {
                <div class="parameter-row" [formGroupName]="index">
                  <mat-form-field appearance="outline" class="parameter-name"><mat-label>Bind variable</mat-label><input matInput formControlName="name" readonly></mat-form-field>
                  <mat-form-field appearance="outline" class="parameter-type">
                    <mat-label>Tipo</mat-label>
                    <mat-select formControlName="type">
                      <mat-option value="VARCHAR2">Texto (VARCHAR2)</mat-option>
                      <mat-option value="NUMBER">Número (NUMBER)</mat-option>
                      <mat-option value="DATE">Data (DATE)</mat-option>
                      <mat-option value="TIMESTAMP">Data e hora (TIMESTAMP)</mat-option>
                    </mat-select>
                  </mat-form-field>
                  <mat-slide-toggle formControlName="required">Obrigatório</mat-slide-toggle>
                </div>
              }
            </div>
          </section>

          @if (error) { <p class="form-error" role="alert">{{ error }}</p> }
          <div class="form-actions">
            <button mat-button type="button" (click)="cancel.emit()">{{ readOnly ? 'Voltar' : 'Cancelar' }}</button>
            @if (!readOnly) { <button mat-flat-button color="primary" [disabled]="form.invalid || saving">{{ saving ? 'Salvando…' : endpoint?.id ? 'Salvar alterações' : 'Criar endpoint' }}</button> }
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .editor-card{max-width:1100px;margin:0 auto}.form-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px 18px;padding-top:20px}.form-grid mat-form-field{width:100%}.wide{grid-column:1/-1}.sql-field textarea{font-family:Consolas,'Courier New',monospace;font-size:.88rem;line-height:1.5}.toggle-cell{display:flex;align-items:center;gap:18px;min-height:56px}.method-chip{padding:5px 9px;border-radius:6px;background:var(--ihub-info-container);color:var(--ihub-info);font-size:.68rem;font-weight:800;letter-spacing:.04em}.parameters-section{margin-top:10px;padding-top:18px;border-top:1px solid var(--ihub-border)}.parameters-heading{display:flex;align-items:center;justify-content:space-between;gap:14px;margin-bottom:14px}.parameters-heading h3{margin:0;font-size:1rem}.parameters-heading p{margin:5px 0 0;color:var(--ihub-muted);font-size:.8rem}.parameter-list{display:grid;gap:4px}.parameter-row{display:grid;grid-template-columns:minmax(180px,1fr) minmax(220px,1fr) 180px;align-items:center;gap:12px}.parameter-row mat-form-field{width:100%}.parameter-empty{padding:18px;border-radius:9px;background:var(--ihub-surface-container);color:var(--ihub-muted);font-size:.82rem}.form-actions{display:flex;justify-content:flex-end;gap:8px;margin-top:16px}.form-error{color:var(--ihub-danger);margin:0 0 10px}@media(max-width:760px){.form-grid{grid-template-columns:1fr}.wide{grid-column:auto}.parameter-row{grid-template-columns:1fr}.parameters-heading{align-items:flex-start;flex-direction:column}.parameters-heading button{width:100%}}
  `],
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
