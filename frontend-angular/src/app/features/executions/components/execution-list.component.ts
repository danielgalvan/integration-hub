import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { Execution, Page } from '../../../core/models';

@Component({
  selector: 'ihub-execution-list',
  standalone: true,
  imports: [DatePipe, MatButtonModule, MatIconModule, MatPaginatorModule, MatProgressBarModule, MatTableModule],
  template: `
    <section class="table-card">
      @if (loading) { <mat-progress-bar mode="indeterminate" /> }
      @if (page.content.length) {
        <div class="table-scroll"><table mat-table [dataSource]="page.content" class="execution-table">
          <ng-container matColumnDef="id"><th mat-header-cell *matHeaderCellDef>ID</th><td mat-cell *matCellDef="let item">#{{ item.id }}</td></ng-container>
          <ng-container matColumnDef="started"><th mat-header-cell *matHeaderCellDef>Data / hora</th><td mat-cell *matCellDef="let item">{{ item.startedAt | date:'dd/MM/yyyy HH:mm:ss' }}</td></ng-container>
          <ng-container matColumnDef="integration"><th mat-header-cell *matHeaderCellDef>Integração</th><td mat-cell *matCellDef="let item">{{ item.integrationName || '—' }}</td></ng-container>
          <ng-container matColumnDef="endpoint"><th mat-header-cell *matHeaderCellDef>Endpoint</th><td mat-cell *matCellDef="let item">{{ item.endpointName || '—' }}</td></ng-container>
          <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let item"><span class="status" [class.success]="item.executionStatus === 'SUCCESS'" [class.error]="item.executionStatus === 'ERROR'" [class.warning]="item.executionStatus === 'UNAUTHORIZED' || item.executionStatus === 'NOT_FOUND'">{{ statusLabel(item.executionStatus) }}</span></td></ng-container>
          <ng-container matColumnDef="http"><th mat-header-cell *matHeaderCellDef>HTTP</th><td mat-cell *matCellDef="let item">{{ item.httpStatus ?? '—' }}</td></ng-container>
          <ng-container matColumnDef="duration"><th mat-header-cell *matHeaderCellDef>Duração</th><td mat-cell *matCellDef="let item">{{ item.durationMs == null ? '—' : item.durationMs + ' ms' }}</td></ng-container>
          <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef>Ações</th><td mat-cell *matCellDef="let item"><button mat-stroked-button type="button" (click)="details.emit(item)"><mat-icon>visibility</mat-icon>Detalhes</button></td></ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr><tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table></div>
      } @else if (!loading) {
        <section class="empty-state"><mat-icon>history</mat-icon><h3>Nenhuma execução encontrada</h3><p>Altere os filtros ou atualize o histórico.</p></section>
      }
      <mat-paginator [length]="page.totalElements" [pageIndex]="page.page" [pageSize]="page.size" [pageSizeOptions]="[10, 20, 50, 100]" [disabled]="loading" (page)="pageChange.emit($event)" />
    </section>
  `,
  styles: [`
    .table-card{overflow:hidden;border:1px solid #eaecf0;border-radius:14px;background:#fff;box-shadow:0 2px 6px #1018280a}.table-scroll{overflow:auto}.execution-table{width:100%;min-width:980px}.execution-table th{white-space:nowrap}.status{display:inline-block;padding:4px 9px;border-radius:99px;background:#f2f4f7;color:#475467;font-size:.73rem;font-weight:700;white-space:nowrap}.status.success{background:#ecfdf3;color:#067647}.status.error{background:#fef3f2;color:#b42318}.status.warning{background:#fffaeb;color:#b54708}.empty-state{display:grid;justify-items:center;padding:50px 20px;text-align:center}.empty-state mat-icon{color:#98a2b3;font-size:40px}.empty-state h3{margin:12px 0 4px}.empty-state p{margin:0;color:#667085}
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExecutionListComponent {
  @Input() page: Page<Execution> = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
  @Input() loading = false;
  @Output() details = new EventEmitter<Execution>();
  @Output() pageChange = new EventEmitter<PageEvent>();
  readonly columns = ['id', 'started', 'integration', 'endpoint', 'status', 'http', 'duration', 'actions'];

  statusLabel(status: string): string {
    return ({ SUCCESS: 'Sucesso', ERROR: 'Erro', UNAUTHORIZED: 'Não autorizado', NOT_FOUND: 'Não encontrado' })[status] || status || '—';
  }
}
