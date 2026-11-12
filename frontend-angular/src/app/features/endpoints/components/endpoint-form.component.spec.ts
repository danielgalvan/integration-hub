import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { EndpointFormComponent } from './endpoint-form.component';

describe('EndpointFormComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [EndpointFormComponent] });
  });

  it('extracts SQL bind variables and emits an endpoint payload', () => {
    const fixture = TestBed.createComponent(EndpointFormComponent);
    fixture.componentRef.setInput('integrationId', 12);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.form.controls.name.setValue('Buscar pedido');
    component.form.controls.path.setValue('/buscar');
    component.form.controls.sqlText.setValue('select id from pedido where id = :id and status = :status and limit = :__ih_max_results');
    component.generateParameters();
    expect(component.parameters.getRawValue().map(parameter => parameter.name)).toEqual(['id', 'status']);
    component.parameters.at(0).controls.type.setValue('NUMBER');
    component.parameters.at(0).controls.required.setValue(true);
    const saved: unknown[] = [];
    component.saved.subscribe(value => saved.push(value));

    component.submit();

    expect(saved).toEqual([{
      integrationId: 12,
      name: 'Buscar pedido',
      description: '',
      path: '/buscar',
      method: 'GET',
      sqlText: 'select id from pedido where id = :id and status = :status and limit = :__ih_max_results',
      active: 'S',
      parameters: [
        { name: 'id', type: 'NUMBER', required: true },
        { name: 'status', type: 'VARCHAR2', required: false },
      ],
    }]);
    fixture.destroy();
  });

  it('blocks saving after SQL bind variables change without regenerating parameters', () => {
    const fixture = TestBed.createComponent(EndpointFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.form.controls.name.setValue('Buscar pedido');
    component.form.controls.path.setValue('/buscar');
    component.form.controls.sqlText.setValue('select id from pedido where id = :id');
    component.generateParameters();
    component.form.controls.sqlText.setValue('select id from pedido where status = :status');
    const validationErrors: string[] = [];
    const saved: unknown[] = [];
    component.validationError.subscribe(message => validationErrors.push(message));
    component.saved.subscribe(value => saved.push(value));

    component.submit();

    expect(component.form.controls.sqlText.hasError('parametersOutOfSync')).toBe(true);
    expect(validationErrors.at(-1)).toContain('Gere novamente os parâmetros');
    expect(saved).toEqual([]);
    fixture.destroy();
  });
});
