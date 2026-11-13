import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { Execution } from '../models/execution.models';
import { Page } from '../../../core/models/common.models';

@Component({
  selector: 'ihub-execution-list',
  standalone: true,
  imports: [DatePipe, MatButtonModule, MatIconModule, MatPaginatorModule, MatProgressBarModule, MatTableModule],
  templateUrl: './execution-list.component.html',
  styleUrl: './execution-list.component.scss',
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
