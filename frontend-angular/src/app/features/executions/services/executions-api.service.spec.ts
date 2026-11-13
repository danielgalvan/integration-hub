import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { Execution } from '../models/execution.models';
import { ExecutionsApiService } from './executions-api.service';

const execution: Execution = {
  id: 11,
  integrationId: 4,
  endpointId: 8,
  startedAt: '2026-11-13T10:30:00',
  executionStatus: 'SUCCESS',
};

describe('ExecutionsApiService', () => {
  let api: ExecutionsApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(ExecutionsApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads history with non-empty filters and preserves zero-valued pagination', () => {
    api.executions({ integrationId: '4', executionStatus: 'ERROR', page: 0, size: 20, endpointId: '' }).subscribe();
    const request = http.expectOne(candidate => candidate.url === '/api/admin/executions');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys()).toEqual(['integrationId', 'executionStatus', 'page', 'size']);
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.has('endpointId')).toBe(false);
    request.flush({ content: [execution], page: 0, size: 20, totalElements: 1, totalPages: 1 });
  });

  it('loads an execution detail by ID', () => {
    api.execution(execution.id).subscribe();
    const request = http.expectOne(`/api/admin/executions/${execution.id}`);
    expect(request.request.method).toBe('GET');
    request.flush(execution);
  });
});
