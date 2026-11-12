import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Execution, Page } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class ExecutionsApiService {
  private readonly http = inject(HttpClient);

  executions(filters: Record<string, string | number>): Observable<Page<Execution>> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) params = params.set(key, String(value));
    });
    return this.http.get<Page<Execution>>('/api/admin/executions', { params });
  }

  execution(id: number): Observable<Execution> {
    return this.http.get<Execution>(`/api/admin/executions/${id}`);
  }
}
