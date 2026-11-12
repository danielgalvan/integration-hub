import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AuthType, Integration } from '../../../core/models';

@Component({
  selector: 'ihub-integration-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSlideToggleModule],
  template: `
    <mat-card class="editor-card">
      <mat-card-header>
        <mat-card-title>{{ readOnly ? 'Visualizar integração' : integration?.id ? 'Editar integração' : 'Nova integração' }}</mat-card-title>
        <mat-card-subtitle>{{ readOnly ? 'Confira os dados e as regras de autenticação.' : 'Configure o caminho base e o acesso à integração.' }}</mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="form-grid">
            <mat-form-field appearance="outline">
              <mat-label>Nome</mat-label>
              <input matInput formControlName="name" maxlength="100" [readonly]="readOnly">
              @if (form.controls.name.hasError('required')) { <mat-error>Informe o nome.</mat-error> }
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Base path</mat-label>
              <input matInput formControlName="basePath" placeholder="/api/pedidos" maxlength="200" [readonly]="readOnly">
              @if (form.controls.basePath.hasError('required')) { <mat-error>Informe o base path.</mat-error> }
              @if (form.controls.basePath.hasError('pattern')) { <mat-error>Use /api/nome, sem barra final ou espaços.</mat-error> }
            </mat-form-field>
            <mat-form-field appearance="outline" class="wide">
              <mat-label>Descrição</mat-label>
              <textarea matInput formControlName="description" rows="3" maxlength="500" [readonly]="readOnly"></textarea>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Autenticação do consumidor</mat-label>
              <mat-select formControlName="authType">
                <mat-option value="NONE">Sem autenticação</mat-option>
                <mat-option value="API_KEY">API Key</mat-option>
              </mat-select>
              <mat-hint>{{ form.controls.authType.value === 'API_KEY' ? 'Os endpoints exigirão X-API-Key.' : 'Os endpoints poderão ser consumidos sem API Key.' }}</mat-hint>
            </mat-form-field>
            <div class="toggle-cell"><mat-slide-toggle formControlName="active">Integração ativa</mat-slide-toggle></div>
          </div>
          @if (formError) { <p class="form-error" role="alert">{{ formError }}</p> }
          <div class="actions">
            <button mat-button type="button" (click)="cancel.emit()">{{ readOnly ? 'Voltar' : 'Cancelar' }}</button>
            @if (!readOnly) { <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving">{{ saving ? 'Salvando…' : integration?.id ? 'Salvar alterações' : 'Criar integração' }}</button> }
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .editor-card{max-width:980px;margin:0 auto}.form-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px 18px;padding-top:20px}.form-grid mat-form-field{width:100%}.wide{grid-column:1/-1}.toggle-cell{display:flex;align-items:center;min-height:56px}.actions{display:flex;justify-content:flex-end;gap:8px;margin-top:8px}.form-error{color:var(--ihub-danger);margin:0 0 12px}@media(max-width:680px){.form-grid{grid-template-columns:1fr}.wide{grid-column:auto}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IntegrationFormComponent implements OnChanges {
  @Input() integration: Integration | null = null;
  @Input() readOnly = false;
  @Input() saving = false;
  @Input() formError = '';
  @Output() saved = new EventEmitter<Integration>();
  @Output() cancel = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    basePath: ['', [Validators.required, Validators.maxLength(200), Validators.pattern(/^\/api\/[^\s]+(?<!\/$)/)]],
    description: ['', Validators.maxLength(500)],
    authType: ['NONE' as AuthType, Validators.required],
    active: [true],
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['integration'] || changes['readOnly']) this.resetForm();
    if (changes['readOnly']) this.toggleDisabledState();
  }

  submit(): void {
    if (this.readOnly || this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.saved.emit({
      id: this.integration?.id,
      name: value.name.trim(),
      basePath: value.basePath.trim(),
      description: value.description.trim(),
      authType: value.authType,
      active: value.active ? 'S' : 'N',
    });
  }

  private resetForm(): void {
    this.form.reset({
      name: this.integration?.name || '',
      basePath: this.integration?.basePath || '',
      description: this.integration?.description || '',
      authType: this.integration?.authType || 'NONE',
      active: this.integration?.active !== 'N',
    });
    this.toggleDisabledState();
  }

  private toggleDisabledState(): void {
    if (this.readOnly) this.form.disable({ emitEvent: false });
    else this.form.enable({ emitEvent: false });
  }
}
