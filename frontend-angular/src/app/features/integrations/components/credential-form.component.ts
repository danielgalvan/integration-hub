import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Credential } from '../../../core/models';

@Component({
  selector: 'ihub-credential-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <section class="form-card">
      <div class="heading">
        <h3>{{ credential ? 'Renomear credencial' : 'Nova credencial' }}</h3>
        <p>{{ credential ? 'Altere o nome usado para identificar este consumidor.' : 'Será gerada uma API Key exclusiva para esta credencial.' }}</p>
      </div>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nome da credencial</mat-label>
          <input matInput formControlName="name" maxlength="100" placeholder="Ex.: Sistema financeiro">
          @if (form.controls.name.hasError('required')) { <mat-error>Informe um nome.</mat-error> }
        </mat-form-field>
        @if (error) { <p class="error" role="alert">{{ error }}</p> }
        <div class="actions">
          <button mat-button type="button" [disabled]="saving" (click)="cancel.emit()">Cancelar</button>
          <button mat-flat-button color="primary" [disabled]="form.invalid || saving">{{ saving ? 'Salvando…' : 'Salvar credencial' }}</button>
        </div>
      </form>
    </section>
  `,
  styles: [`
    .form-card{padding:18px;border:1px solid #eaecf0;border-radius:12px;background:#f9fafb}.heading h3{margin:0;font-size:1rem}.heading p{margin:5px 0 14px;color:#667085;font-size:.82rem}.full-width{width:100%}.actions{display:flex;justify-content:flex-end;gap:8px}.error{color:#b42318}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CredentialFormComponent implements OnChanges {
  @Input() credential: Credential | null = null;
  @Input() saving = false;
  @Input() error = '';
  @Output() saved = new EventEmitter<string>();
  @Output() cancel = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  readonly form = this.fb.nonNullable.group({ name: ['', [Validators.required, Validators.maxLength(100)]] });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['credential']) this.form.reset({ name: this.credential?.name || '' });
  }

  submit(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saved.emit(this.form.controls.name.value.trim());
  }
}
