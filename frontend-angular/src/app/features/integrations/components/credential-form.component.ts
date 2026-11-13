import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CredentialDraft } from '../models/integration.models';

@Component({
  selector: 'ihub-credential-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './credential-form.component.html',
  styles: [`
    .form-card{padding:18px;border:1px solid var(--ihub-border);border-radius:12px;background:var(--ihub-surface-container)}.heading h3{margin:0;font-size:1rem}.heading p{margin:5px 0 14px;color:var(--ihub-muted);font-size:.82rem}.full-width{width:100%}.actions{display:flex;justify-content:flex-end;gap:8px}.error{color:var(--ihub-danger)}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CredentialFormComponent implements OnChanges {
  @Input() credential: CredentialDraft | null = null;
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
