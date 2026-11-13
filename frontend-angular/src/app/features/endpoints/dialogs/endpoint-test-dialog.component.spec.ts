import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Endpoint } from '../models/endpoint.models';
import { Integration } from '../../integrations/models/integration.models';
import { EndpointTestDialogComponent } from './endpoint-test-dialog.component';
import { EndpointsApiService } from '../services/endpoints-api.service';

const integration: Integration = {
  id: 2,
  name: 'Pedidos',
  basePath: '/api/pedidos',
  active: 'S',
  authType: 'NONE',
};

const endpoint: Endpoint = {
  id: 3,
  integrationId: 2,
  name: 'Buscar pedido',
  path: '/buscar',
  method: 'GET',
  sqlText: 'select id from pedido where id = :id',
  parameters: [
    { name: 'id', type: 'NUMBER', required: true },
    { name: 'data', type: 'DATE', required: false },
  ],
  active: 'S',
};

describe('EndpointTestDialogComponent', () => {
  let api: { endpointUrl: ReturnType<typeof vi.fn>; execute: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    api = {
      endpointUrl: vi.fn(() => '/api/pedidos/buscar?id=42'),
      execute: vi.fn(() => of(new HttpResponse({
        body: '{"rows":[{"ID":42}]}',
        headers: new HttpHeaders({ 'content-type': 'application/json' }),
        status: 200,
        statusText: 'OK',
        url: '/api/pedidos/buscar?id=42',
      }))),
    };
    TestBed.configureTestingModule({
      imports: [EndpointTestDialogComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { integration, endpoint } },
        { provide: MatDialogRef, useValue: { close: vi.fn() } },
        { provide: EndpointsApiService, useValue: api },
      ],
    });
  });

  it('blocks invalid typed parameters and executes with validated values', () => {
    const fixture = TestBed.createComponent(EndpointTestDialogComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.parametersForm.controls['id'].setValue('not-a-number');
    component.parametersForm.controls['data'].setValue('2026-02-29');
    component.execute();
    expect(component.parametersForm.invalid).toBe(true);
    expect(api.execute).not.toHaveBeenCalled();

    component.parametersForm.controls['id'].setValue('42');
    component.parametersForm.controls['data'].setValue('2024-02-29');
    component.execute();

    expect(api.execute).toHaveBeenCalledWith(integration, endpoint, { id: '42', data: '2024-02-29' }, '');
    expect(component.result()?.success).toBe(true);
    expect(component.result()?.data).toEqual({ rows: [{ ID: 42 }] });
    fixture.destroy();
  });

  it('requires an API key for an API_KEY integration', () => {
    const keyIntegration = { ...integration, authType: 'API_KEY' as const };
    TestBed.overrideProvider(MAT_DIALOG_DATA, { useValue: { integration: keyIntegration, endpoint } });
    const fixture = TestBed.createComponent(EndpointTestDialogComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.parametersForm.controls['id'].setValue('42');
    component.execute();
    expect(component.apiKey.invalid).toBe(true);
    expect(api.execute).not.toHaveBeenCalled();

    component.apiKey.setValue('ihub_test_key');
    component.execute();
    expect(api.execute).toHaveBeenCalledWith(keyIntegration, endpoint, { id: '42', data: '' }, 'ihub_test_key');
    fixture.destroy();
  });

  it('uses a numeric keyboard only for NUMBER parameters', () => {
    const fixture = TestBed.createComponent(EndpointTestDialogComponent);
    const component = fixture.componentInstance;

    expect(component.inputMode('NUMBER')).toBe('decimal');
    expect(component.inputMode('DATE')).toBeNull();
    expect(component.inputMode('TIMESTAMP')).toBeNull();

    fixture.destroy();
  });
});
