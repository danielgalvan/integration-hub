import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { ExecutionFiltersComponent } from './execution-filters.component';

describe('ExecutionFiltersComponent', () => {
  it('enables endpoint selection only when an integration is selected and endpoints are not loading', () => {
    const fixture = TestBed.createComponent(ExecutionFiltersComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const endpointControl = component.form.controls.endpointId;

    expect(endpointControl.disabled).toBe(true);

    fixture.componentRef.setInput('filters', {
      integrationId: '7',
      endpointId: '',
      executionStatus: '',
      startedFrom: '',
      startedTo: '',
    });
    fixture.detectChanges();

    expect(endpointControl.enabled).toBe(true);

    fixture.componentRef.setInput('loadingEndpoints', true);
    fixture.detectChanges();

    expect(endpointControl.disabled).toBe(true);

    fixture.componentRef.setInput('loadingEndpoints', false);
    fixture.detectChanges();

    expect(endpointControl.enabled).toBe(true);

    fixture.componentRef.setInput('filters', {
      integrationId: '',
      endpointId: '',
      executionStatus: '',
      startedFrom: '',
      startedTo: '',
    });
    fixture.detectChanges();

    expect(endpointControl.disabled).toBe(true);
    fixture.destroy();
  });
});
