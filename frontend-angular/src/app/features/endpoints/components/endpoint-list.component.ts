import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Endpoint } from '../../../core/models';

@Component({
  selector: 'ihub-endpoint-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTableModule, MatTooltipModule],
  template: `
    @if (endpoints.length) {
      <div class="table-card">
        <table mat-table [dataSource]="endpoints" class="endpoint-table">
          <ng-container matColumnDef="endpoint">
            <th mat-header-cell *matHeaderCellDef>Endpoint</th>
            <td mat-cell *matCellDef="let item"><strong>{{ item.name }}</strong><small>{{ item.description || 'Sem descrição' }}</small><code>{{ item.method }} {{ item.path }}</code></td>
          </ng-container>
          <ng-container matColumnDef="parameters">
            <th mat-header-cell *matHeaderCellDef>Parâmetros</th>
            <td mat-cell *matCellDef="let item">{{ item.parameters?.length || 0 }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let item"><span class="status" [class.inactive]="item.active !== 'S'">{{ item.active === 'S' ? 'Ativo' : 'Inativo' }}</span></td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Ações</th>
            <td mat-cell *matCellDef="let item" class="actions">
              <button mat-stroked-button type="button" [disabled]="item.active !== 'S'" (click)="test.emit(item)"><mat-icon>play_arrow</mat-icon>Testar</button>
              <button mat-icon-button type="button" [matTooltip]="canEdit ? 'Editar endpoint' : 'Visualizar endpoint'" (click)="edit.emit(item)"><mat-icon>{{ canEdit ? 'edit' : 'visibility' }}</mat-icon></button>
              @if (canEdit) { <button mat-icon-button color="warn" type="button" matTooltip="Excluir endpoint" (click)="remove.emit(item)"><mat-icon>delete_outline</mat-icon></button> }
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </div>
    } @else if (!loading) {
      <section class="empty-card"><mat-icon>api</mat-icon><h3>Nenhum endpoint cadastrado</h3><p>Adicione um endpoint GET para publicar uma consulta de leitura.</p></section>
    }
  `,
  styles: [`
    .table-card {
      overflow: auto;
      border: 1px solid var(--ihub-border);
      border-radius: 14px;
      background: var(--ihub-surface);
      box-shadow: 0 2px 6px color-mix(in srgb, var(--ihub-text) 4%, transparent);
    }
    .endpoint-table { width: 100%; min-width: 740px; }
    .endpoint-table .mat-mdc-header-cell {
      padding: 14px 16px;
      color: var(--ihub-text-secondary);
      font-weight: 600;
    }
    .endpoint-table .mat-mdc-cell {
      padding: 14px 16px;
      border-bottom: 1px solid var(--ihub-border);
      vertical-align: middle;
    }
    .endpoint-table .mat-mdc-row:last-child .mat-mdc-cell { border-bottom: 0; }
    .mat-mdc-cell small,
    .mat-mdc-cell code { display: block; margin-top: 8px; }
    .mat-mdc-cell small {
      max-width: 420px;
      color: var(--ihub-muted);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .mat-mdc-cell code {
      width: max-content;
      max-width: 100%;
      padding: 4px 7px;
      border-radius: 5px;
      background: var(--ihub-surface-subtle);
      color: var(--ihub-text-secondary);
      font-size: .72rem;
    }
    .status {
      display: inline-block;
      padding: 4px 9px;
      border-radius: 99px;
      background: var(--ihub-success-container);
      color: var(--ihub-success);
      font-size: .74rem;
      font-weight: 700;
    }
    .status.inactive { background: var(--ihub-danger-container); color: var(--ihub-danger); }
    .actions { text-align: right; white-space: nowrap; }
    .actions button[mat-stroked-button] { font-size: .75rem; }
    .empty-card {
      display: grid;
      justify-items: center;
      padding: 54px 20px;
      border: 1px dashed var(--ihub-outline);
      border-radius: 14px;
      background: var(--ihub-surface);
      text-align: center;
    }
    .empty-card > mat-icon { color: var(--ihub-muted); font-size: 38px; }
    .empty-card h3 { margin: 12px 0 4px; font-size: 1rem; }
    .empty-card p { margin: 0; color: var(--ihub-muted); }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EndpointListComponent {
  @Input() endpoints: Endpoint[] = [];
  @Input() canEdit = false;
  @Input() loading = false;
  @Output() test = new EventEmitter<Endpoint>();
  @Output() edit = new EventEmitter<Endpoint>();
  @Output() remove = new EventEmitter<Endpoint>();
  readonly columns = ['endpoint', 'parameters', 'status', 'actions'];
}
