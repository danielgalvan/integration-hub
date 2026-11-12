import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { ApiService } from '../../core/api.service';
import { Integration } from '../../core/models';
import { SessionService } from '../../core/session.service';
import { ConfirmationService } from '../../shared/dialogs/confirmation.service';
import { IntegrationsComponent } from './integrations.component';

const existingIntegration: Integration = {
  id: 5,
  name: 'Pedidos',
  basePath: '/api/pedidos',
  active: 'S',
  authType: 'NONE',
};

describe('IntegrationsComponent', () => {
  it('carrega e exibe as integrações retornadas pela API', () => {
    const api = {
      integrations: vi.fn(() => of([existingIntegration])),
      createIntegration: vi.fn(() => of(existingIntegration)),
      updateIntegration: vi.fn(() => of(existingIntegration)),
      deleteIntegration: vi.fn(() => of(void 0)),
      generateIntegrationApiKey: vi.fn(() => of({ apiKey: 'key' })),
    };
    const dialog = { open: vi.fn() };
    const snack = { open: vi.fn() };
    const confirmation = { confirm: vi.fn(() => of(false)) };
    TestBed.configureTestingModule({
      imports: [IntegrationsComponent],
      providers: [
        provideRouter([]),
        { provide: ApiService, useValue: api },
        { provide: SessionService, useValue: { canEdit: () => true } },
        { provide: MatDialog, useValue: { open: vi.fn() } },
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
        { provide: ConfirmationService, useValue: { confirm: vi.fn(() => of(false)) } },
      ],
    });
    TestBed.overrideComponent(IntegrationsComponent, {
      set: {
        providers: [
          { provide: MatDialog, useValue: dialog },
          { provide: MatSnackBar, useValue: snack },
          { provide: ConfirmationService, useValue: confirmation },
        ],
      },
    });

    const fixture = TestBed.createComponent(IntegrationsComponent);
    fixture.detectChanges();

    expect(api.integrations).toHaveBeenCalledOnce();
    expect(fixture.componentInstance.items()).toEqual([existingIntegration]);
    expect(fixture.nativeElement.textContent).toContain('Pedidos');
    fixture.destroy();
  });

  it('salva uma nova integração e recarrega a listagem', () => {
    const api = {
      integrations: vi.fn(() => of([])),
      createIntegration: vi.fn(() => of({ ...existingIntegration, id: 6 })),
      updateIntegration: vi.fn(() => of(existingIntegration)),
      deleteIntegration: vi.fn(() => of(void 0)),
      generateIntegrationApiKey: vi.fn(() => of({ apiKey: 'key' })),
    };
    const snack = { open: vi.fn() };
    const dialog = { open: vi.fn() };
    const confirmation = { confirm: vi.fn(() => of(false)) };
    TestBed.configureTestingModule({
      imports: [IntegrationsComponent],
      providers: [
        provideRouter([]),
        { provide: ApiService, useValue: api },
        { provide: SessionService, useValue: { canEdit: () => true } },
        { provide: MatDialog, useValue: { open: vi.fn() } },
        { provide: MatSnackBar, useValue: snack },
        { provide: ConfirmationService, useValue: { confirm: vi.fn(() => of(false)) } },
      ],
    });
    TestBed.overrideComponent(IntegrationsComponent, {
      set: {
        providers: [
          { provide: MatDialog, useValue: dialog },
          { provide: MatSnackBar, useValue: snack },
          { provide: ConfirmationService, useValue: confirmation },
        ],
      },
    });

    const fixture = TestBed.createComponent(IntegrationsComponent);
    const component = fixture.componentInstance;
    const created: Integration = {
      name: 'Clientes',
      basePath: '/api/clientes',
      active: 'S',
      authType: 'NONE',
    };

    component.newIntegration();
    component.save(created);

    expect(api.createIntegration).toHaveBeenCalledWith(created);
    expect(api.integrations).toHaveBeenCalledTimes(2);
    expect(component.selected()).toBeNull();
    expect(snack.open).toHaveBeenCalledWith(
      'Integração salva com sucesso.',
      'Fechar',
      { duration: 2500 },
    );
    fixture.destroy();
  });
});
