import { TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { Page } from '../../../core/models/common.models';
import { Execution } from '../models/execution.models';
import { ExecutionsComponent } from './executions.component';
import { EndpointsApiService } from '../../endpoints/services/endpoints-api.service';
import { IntegrationsApiService } from '../../integrations/services/integrations-api.service';
import { ExecutionsApiService } from '../services/executions-api.service';

describe('ExecutionsComponent', () => {
  it('ignores an older history response after a newer filter search', () => {
    const staleResponse = new Subject<Page<Execution>>();
    const currentResponse = new Subject<Page<Execution>>();
    const api = {
      integrations: vi.fn(() => of([])),
      executions: vi.fn()
        .mockReturnValueOnce(staleResponse)
        .mockReturnValueOnce(currentResponse),
      endpoints: vi.fn(() => of([])),
    };
    TestBed.configureTestingModule({
      imports: [ExecutionsComponent],
      providers: [
        { provide: EndpointsApiService, useValue: api },
        { provide: IntegrationsApiService, useValue: api },
        { provide: ExecutionsApiService, useValue: api },
      ],
    });
    const fixture = TestBed.createComponent(ExecutionsComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.updateFilters({
      integrationId: '8',
      endpointId: '',
      executionStatus: 'ERROR',
      startedFrom: '',
      startedTo: '',
    });
    component.search();

    const latest: Page<Execution> = { content: [], page: 0, size: 20, totalElements: 2, totalPages: 1 };
    currentResponse.next(latest);
    staleResponse.next({ ...latest, totalElements: 99, totalPages: 5 });

    expect(component.page()).toEqual(latest);
    expect(api.executions).toHaveBeenCalledTimes(2);
    expect(api.executions.mock.calls[1][0]).toMatchObject({ integrationId: '8', executionStatus: 'ERROR', page: 0, size: 20 });
    fixture.destroy();
  });
});
