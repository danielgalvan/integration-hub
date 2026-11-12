import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';

export interface SecretDialogData {
  title: string;
  description: string;
  warning: string;
  secret: string;
  label?: string;
}

@Component({
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p>{{ data.description }}</p>
      <section class="warning"><mat-icon>info</mat-icon><span>{{ data.warning }}</span></section>
      <label class="secret-label">{{ data.label || 'Valor gerado' }}</label>
      <div class="secret-value">
        <code>{{ data.secret }}</code>
        <button mat-stroked-button type="button" (click)="copy()">
          <mat-icon>{{ copied() ? 'check' : 'content_copy' }}</mat-icon>
          {{ copied() ? 'Copiado' : 'Copiar' }}
        </button>
      </div>
      @if (data.label === 'API Key') {
        <p class="header-label">Header para consumo</p>
        <code class="header-value">X-API-Key: {{ data.secret }}</code>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-flat-button color="primary" mat-dialog-close>Entendi</button></mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content{display:grid;gap:14px}.warning{display:flex;gap:10px;padding:12px;border-radius:9px;background:var(--ihub-warning-container);color:var(--ihub-warning);font-size:.88rem}.secret-label,.header-label{margin:0;color:var(--ihub-text-secondary);font-size:.78rem;font-weight:700}.secret-value{display:flex;align-items:center;gap:10px;padding:10px;border:1px solid var(--ihub-outline);border-radius:10px;background:var(--ihub-surface-container)}.secret-value code{flex:1;min-width:0;overflow-wrap:anywhere;font-size:.9rem}.secret-value button{flex:none}.header-value{display:block;padding:10px;background:var(--ihub-surface-subtle);border-radius:8px;overflow-wrap:anywhere;font-size:.76rem}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecretDialogComponent {
  readonly copied = signal(false);
  readonly data = inject<SecretDialogData>(MAT_DIALOG_DATA);
  private readonly snack = inject(MatSnackBar);

  async copy(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.data.secret);
      this.copied.set(true);
      this.snack.open('Copiado para a área de transferência.', 'Fechar', { duration: 1800 });
    } catch {
      this.snack.open('Não foi possível copiar. Selecione e copie o valor manualmente.', 'Fechar');
    }
  }
}
