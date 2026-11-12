import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { IntegrationFormComponent } from './integration-form.component';

describe('IntegrationFormComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [IntegrationFormComponent] });
  });

  it('validates the base path and emits a normalized integration payload', () => {
    const fixture = TestBed.createComponent(IntegrationFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const saved: unknown[] = [];
    component.saved.subscribe(value => saved.push(value));

    component.form.setValue({
      name: '  Pedidos  ',
      basePath: '/api/pedidos',
      description: '  Consulta de pedidos  ',
      authType: 'API_KEY',
      active: false,
    });
    component.submit();

    expect(saved).toEqual([{
      name: 'Pedidos',
      basePath: '/api/pedidos',
      description: 'Consulta de pedidos',
      authType: 'API_KEY',
      active: 'N',
    }]);
    fixture.destroy();
  });

  it('rejects a base path outside the /api namespace and does not emit', () => {
    const fixture = TestBed.createComponent(IntegrationFormComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const saved: unknown[] = [];
    component.saved.subscribe(value => saved.push(value));
    component.form.setValue({ name: 'Pedidos', basePath: '/pedidos', description: '', authType: 'NONE', active: true });

    component.submit();

    expect(component.form.controls.basePath.hasError('pattern')).toBe(true);
    expect(saved).toEqual([]);
    fixture.destroy();
  });
});
