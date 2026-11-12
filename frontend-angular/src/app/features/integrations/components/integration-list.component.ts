import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Integration } from '../../../core/models';

@Component({
  selector: 'ihub-integration-list',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTableModule, MatTooltipModule],
  template: `
    @if (integrations.length) {
      <div class="table-card">
        <table mat-table [dataSource]="integrations" class="integration-table">
          <ng-container matColumnDef="integration">
            <th mat-header-cell *matHeaderCellDef>Integração</th>
            <td mat-cell *matCellDef="let item">
              <strong>{{ item.name }}</strong>
              @if (item.description) { <small>{{ item.description }}</small> }
              <code>{{ item.basePath }}</code>
            </td>
          </ng-container>
          <ng-container matColumnDef="authentication">
            <th mat-header-cell *matHeaderCellDef>Acesso</th>
            <td mat-cell *matCellDef="let item">
              <span class="auth-type"><mat-icon>{{ item.authType === 'API_KEY' ? 'key' : 'public' }}</mat-icon>{{ item.authType === 'API_KEY' ? 'API Key' : 'Pública' }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let item"><span class="status" [class.inactive]="item.active !== 'S'">{{ item.active === 'S' ? 'Ativa' : 'Inativa' }}</span></td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Ações</th>
            <td mat-cell *matCellDef="let item" class="actions">
              <button mat-stroked-button type="button" (click)="endpoints.emit(item)"><mat-icon>route</mat-icon>Endpoints</button>
              <button mat-icon-button type="button" [matTooltip]="canEdit ? 'Editar integração' : 'Visualizar integração'" (click)="edit.emit(item)"><mat-icon>{{ canEdit ? 'edit' : 'visibility' }}</mat-icon></button>
              @if (canEdit && item.authType === 'API_KEY') {
                <button mat-icon-button type="button" matTooltip="Gerenciar credenciais" (click)="credentials.emit(item)"><mat-icon>vpn_key</mat-icon></button>
                <button mat-icon-button type="button" [matTooltip]="item.apiKeyCreatedAt ? 'Regenerar API Key legada' : 'Gerar API Key legada'" (click)="generateLegacyKey.emit(item)"><mat-icon>key</mat-icon></button>
              }
              @if (canEdit) { <button mat-icon-button type="button" matTooltip="Excluir integração" color="warn" (click)="remove.emit(item)"><mat-icon>delete_outline</mat-icon></button> }
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      </div>
    } @else if (!loading) {
      <section class="empty-card">
        <mat-icon>hub</mat-icon>
        <h2>Nenhuma integração cadastrada</h2>
        <p>Crie uma integração para agrupar endpoints e controlar o acesso.</p>
      </section>
    }
  `,
  styles: [`
    .table-card{overflow:auto;border:1px solid #eaecf0;border-radius:14px;background:#fff;box-shadow:0 2px 6px #1018280a}.integration-table{width:100%;min-width:760px}.mat-mdc-cell small,.mat-mdc-cell code{display:block;margin-top:4px}.mat-mdc-cell small{max-width:420px;color:#667085;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.mat-mdc-cell code{width:max-content;max-width:100%;padding:3px 6px;border-radius:5px;background:#f2f4f7;color:#475467;font-size:.72rem}.auth-type{display:inline-flex;align-items:center;gap:6px;color:#475467}.auth-type mat-icon{width:18px;height:18px;font-size:18px}.status{display:inline-block;padding:4px 9px;border-radius:99px;background:#ecfdf3;color:#067647;font-size:.74rem;font-weight:700}.status.inactive{background:#fef3f2;color:#b42318}.actions{white-space:nowrap;text-align:right}.actions button[mat-stroked-button]{font-size:.75rem}.empty-card{display:grid;justify-items:center;padding:54px 20px;border:1px dashed #d0d5dd;border-radius:14px;background:#fff;text-align:center}.empty-card>mat-icon{width:48px;height:48px;color:#98a2b3;font-size:42px}.empty-card h2{margin:12px 0 4px;font-size:1.05rem}.empty-card p{margin:0;color:#667085}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IntegrationListComponent {
  @Input() integrations: Integration[] = [];
  @Input() canEdit = false;
  @Input() loading = false;
  @Output() endpoints = new EventEmitter<Integration>();
  @Output() edit = new EventEmitter<Integration>();
  @Output() credentials = new EventEmitter<Integration>();
  @Output() generateLegacyKey = new EventEmitter<Integration>();
  @Output() remove = new EventEmitter<Integration>();
  readonly columns = ['integration', 'authentication', 'status', 'actions'];
}
