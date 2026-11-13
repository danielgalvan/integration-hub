import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { Endpoint } from '../models/endpoint.models';
import { Integration } from '../../integrations/models/integration.models';
import { SessionService } from '../../../core/auth/session.service';
import { ConfirmationService } from '../../../shared/dialogs/confirmation.service';
import { EndpointsComponent } from './endpoints.component';
import { EndpointsApiService } from '../services/endpoints-api.service';
import { IntegrationsApiService } from '../../integrations/services/integrations-api.service';

const integration: Integration = {
  id: 42,
  name: 'Clientes',
  basePath: '/api/clientes',
  active: 'S',
  authType: 'NONE',
};

const endpoint: Endpoint = {
  id: 8,
  integrationId: 42,
  name: 'Buscar cliente',
  path: '/buscar',
  method: 'GET',
  sqlText: 'select id from cliente where id = :id',
  parameters: [{ name: 'id', type: 'NUMBER', required: true }],
  active: 'S',
};

describe('EndpointsComponent', () => {
  it('restaura uma rota de endpoints aberta diretamente, sem history.state', () => {
    const api = {
      integrations: vi.fn(() => of([integration])),
      endpoints: vi.fn(() => of([endpoint])),
      createEndpoint: vi.fn(() => of(endpoint)),
      updateEndpoint: vi.fn(() => of(endpoint)),
      deleteEndpoint: vi.fn(() => of(void 0)),
    };
    const dialog = { open: vi.fn() };
    const snack = { open: vi.fn() };
    const confirmation = { confirm: vi.fn(() => of(false)) };
    TestBed.configureTestingModule({
      imports: [EndpointsComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '42' } } },
        },
        { provide: EndpointsApiService, useValue: api },
        { provide: IntegrationsApiService, useValue: api },
        { provide: SessionService, useValue: { canEdit: () => true } },
        { provide: MatDialog, useValue: { open: vi.fn() } },
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
        { provide: ConfirmationService, useValue: { confirm: vi.fn(() => of(false)) } },
      ],
    });
    TestBed.overrideComponent(EndpointsComponent, {
      set: {
        providers: [
          { provide: MatDialog, useValue: dialog },
          { provide: MatSnackBar, useValue: snack },
          { provide: ConfirmationService, useValue: confirmation },
        ],
      },
    });

    const fixture = TestBed.createComponent(EndpointsComponent);
    fixture.detectChanges();

    expect(api.integrations).toHaveBeenCalledOnce();
    expect(api.endpoints).toHaveBeenCalledWith(42);
    expect(fixture.componentInstance.integration()).toEqual(integration);
    expect(fixture.componentInstance.items()).toEqual([endpoint]);
    expect(fixture.nativeElement.textContent).toContain('Buscar cliente');
    fixture.destroy();
  });

  it('cria endpoint na integração selecionada e atualiza a listagem', () => {
    const api = {
      integrations: vi.fn(() => of([integration])),
      endpoints: vi.fn(() => of([endpoint])),
      createEndpoint: vi.fn(() => of(endpoint)),
      updateEndpoint: vi.fn(() => of(endpoint)),
      deleteEndpoint: vi.fn(() => of(void 0)),
    };
    const snack = { open: vi.fn() };
    const dialog = { open: vi.fn() };
    const confirmation = { confirm: vi.fn(() => of(false)) };
    TestBed.configureTestingModule({
      imports: [EndpointsComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '42' } } },
        },
        { provide: EndpointsApiService, useValue: api },
        { provide: IntegrationsApiService, useValue: api },
        { provide: SessionService, useValue: { canEdit: () => true } },
        { provide: MatDialog, useValue: { open: vi.fn() } },
        { provide: MatSnackBar, useValue: snack },
        { provide: ConfirmationService, useValue: { confirm: vi.fn(() => of(false)) } },
      ],
    });
    TestBed.overrideComponent(EndpointsComponent, {
      set: {
        providers: [
          { provide: MatDialog, useValue: dialog },
          { provide: MatSnackBar, useValue: snack },
          { provide: ConfirmationService, useValue: confirmation },
        ],
      },
    });

    const fixture = TestBed.createComponent(EndpointsComponent);
    const component = fixture.componentInstance;
    component.newEndpoint();
    component.save(endpoint);

    expect(api.createEndpoint).toHaveBeenCalledWith(endpoint);
    expect(api.endpoints).toHaveBeenCalledTimes(2);
    expect(component.formOpen()).toBe(false);
    expect(snack.open).toHaveBeenCalledWith(
      'Endpoint salvo com sucesso.',
      'Fechar',
      { duration: 2200 },
    );
    fixture.destroy();
  });
});
