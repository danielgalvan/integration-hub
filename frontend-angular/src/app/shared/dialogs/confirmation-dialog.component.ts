import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
}

@Component({
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div mat-dialog-title class="title-row">
      @if (data.destructive) { <mat-icon color="warn">warning</mat-icon> }
      <span>{{ data.title }}</span>
    </div>
    <mat-dialog-content>{{ data.message }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close="false">{{ data.cancelLabel || 'Cancelar' }}</button>
      <button
        mat-flat-button
        [color]="data.destructive ? 'warn' : 'primary'"
        [mat-dialog-close]="true"
      >{{ data.confirmLabel || 'Confirmar' }}</button>
    </mat-dialog-actions>
  `,
  styles: [`.title-row{display:flex;align-items:center;gap:10px}.title-row mat-icon{color:#b42318}`],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmationDialogComponent {
  readonly data = inject<ConfirmationDialogData>(MAT_DIALOG_DATA);
}
