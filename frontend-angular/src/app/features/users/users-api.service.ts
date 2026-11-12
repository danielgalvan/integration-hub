import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { TemporaryPasswordResponse, User, UserCreateResponse } from '../../core/models';

@Injectable({ providedIn: 'root' })
export class UsersApiService {
  private readonly http = inject(HttpClient);

  users(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }

  createUser(value: Partial<User>): Observable<UserCreateResponse> {
    return this.http.post<UserCreateResponse>('/api/users', value);
  }

  updateUser(id: number, value: Partial<User>): Observable<User> {
    return this.http.put<User>(`/api/users/${id}`, value);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`/api/users/${id}`);
  }

  resetPassword(id: number): Observable<TemporaryPasswordResponse> {
    return this.http.post<TemporaryPasswordResponse>(`/api/users/${id}/reset-password`, {});
  }
}
