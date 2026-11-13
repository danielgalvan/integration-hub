import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { User } from '../models/user.models';
import { UserRole } from '../../../core/models/common.models';

@Component({
  selector: 'ihub-user-form',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserFormComponent implements OnChanges {
  @Input() user: User | null = null;
  @Input() saving = false;
  @Input() error = '';
  @Output() saved = new EventEmitter<Partial<User>>();
  @Output() cancel = new EventEmitter<void>();
  private readonly fb = inject(FormBuilder);
  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(100)]],
    name: ['', [Validators.required, Validators.maxLength(200)]],
    email: ['', [Validators.email, Validators.maxLength(200)]],
    type: ['U' as UserRole, Validators.required],
    status: ['A' as 'A' | 'I', Validators.required],
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['user']) this.form.reset({
      username: this.user?.username || '',
      name: this.user?.name || '',
      email: this.user?.email || '',
      type: this.user?.type || 'U',
      status: this.user?.status || 'A',
    });
  }

  submit(): void {
    if (this.form.invalid || this.saving) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    const payload: Partial<User> = {
      username: value.username.trim(),
      name: value.name.trim(),
      email: value.email.trim() || undefined,
      type: value.type,
    };
    if (this.user) payload.status = value.status;
    this.saved.emit(payload);
  }
}
