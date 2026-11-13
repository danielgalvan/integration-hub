import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { Execution } from '../models/execution.models';
import { ExecutionsApiService } from '../services/executions-api.service';
import { ExecutionDetailDialogComponent } from './execution-detail-dialog.component';

const summary: Execution = {
  id: 18,
  integrationId: 4,
  integrationName: 'Pedidos',
  endpointId: 8,
  endpointName: 'Buscar pedido',
  startedAt: '2026-11-13T10:30:00',
  executionStatus: 'SUCCESS',
};

describe('ExecutionDetailDialogComponent', () => {
  it('loads and displays the requested execution detail', () => {
    const detail = { ...summary, requestPath: '/api/pedidos/buscar?id=18', httpStatus: 200 };
    const api = { execution: vi.fn(() => of(detail)) };
    TestBed.configureTestingModule({
      imports: [ExecutionDetailDialogComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: summary },
        { provide: MatDialogRef, useValue: { close: vi.fn() } },
        { provide: ExecutionsApiService, useValue: api },
      ],
    });

    const fixture = TestBed.createComponent(ExecutionDetailDialogComponent);
    fixture.detectChanges();

    expect(api.execution).toHaveBeenCalledWith(summary.id);
    expect(fixture.nativeElement.textContent).toContain('/api/pedidos/buscar?id=18');
    expect(fixture.nativeElement.textContent).toContain('200');
    expect(fixture.nativeElement.textContent).toContain('Sucesso');
    fixture.destroy();
  });
});
