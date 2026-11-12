import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Credential } from '../../../core/models';

@Component({
  selector: 'ihub-credential-list',
  standalone: true,
  imports: [DatePipe, MatButtonModule, MatIconModule, MatTooltipModule],
  template: `
    @if (!credentials.length) {
      <section class="empty-state"><mat-icon>key_off</mat-icon><h3>Nenhuma credencial cadastrada</h3><p>Crie uma credencial para identificar e controlar cada consumidor da integração.</p></section>
    } @else {
      <div class="credential-list">
        @for (credential of credentials; track credential.id) {
          <article class="credential-row">
            <div class="credential-info">
              <strong>{{ credential.name }}</strong>
              <span>ID {{ credential.id }} · {{ credential.lastUsedAt ? ('Último uso ' + (credential.lastUsedAt | date:'dd/MM/yyyy HH:mm')) : 'Nunca utilizada' }}</span>
            </div>
            <span class="status" [class.inactive]="credential.active !== 'S'">{{ credential.active === 'S' ? 'Ativa' : 'Inativa' }}</span>
            <div class="actions">
              <button mat-icon-button type="button" matTooltip="Consultar API Key" (click)="viewKey.emit(credential)"><mat-icon>visibility</mat-icon></button>
              <button mat-icon-button type="button" matTooltip="Editar nome" (click)="edit.emit(credential)"><mat-icon>edit</mat-icon></button>
              <button mat-icon-button type="button" [matTooltip]="credential.active === 'S' ? 'Desativar credencial' : 'Ativar credencial'" (click)="toggle.emit(credential)"><mat-icon>{{ credential.active === 'S' ? 'toggle_on' : 'toggle_off' }}</mat-icon></button>
              <button mat-icon-button type="button" matTooltip="Regenerar API Key" (click)="regenerate.emit(credential)"><mat-icon>autorenew</mat-icon></button>
            </div>
          </article>
        }
      </div>
    }
  `,
  styles: [`
    .credential-list{display:grid;gap:8px}.credential-row{display:flex;align-items:center;gap:16px;padding:14px 12px;border:1px solid var(--ihub-border);border-radius:10px}.credential-info{display:grid;gap:4px;flex:1;min-width:0}.credential-info strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.credential-info span{color:var(--ihub-muted);font-size:.76rem}.status{padding:4px 8px;border-radius:99px;background:var(--ihub-success-container);color:var(--ihub-success);font-size:.7rem;font-weight:700;white-space:nowrap}.status.inactive{background:var(--ihub-danger-container);color:var(--ihub-danger)}.actions{display:flex;align-items:center}.empty-state{display:grid;justify-items:center;padding:34px 18px;text-align:center;color:var(--ihub-muted)}.empty-state mat-icon{color:var(--ihub-muted);font-size:34px}.empty-state h3{margin:10px 0 3px;color:var(--ihub-text-secondary);font-size:.95rem}.empty-state p{margin:0;font-size:.82rem}@media(max-width:620px){.credential-row{display:grid;grid-template-columns:1fr auto}.credential-info{grid-column:1}.status{grid-column:2;grid-row:1}.actions{grid-column:1/-1;justify-content:flex-end;border-top:1px solid var(--ihub-border);padding-top:7px}}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CredentialListComponent {
  @Input() credentials: Credential[] = [];
  @Output() viewKey = new EventEmitter<Credential>();
  @Output() edit = new EventEmitter<Credential>();
  @Output() toggle = new EventEmitter<Credential>();
  @Output() regenerate = new EventEmitter<Credential>();
}
