import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthUser, Environment, LoginResponse } from './models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  environments(): Observable<Environment[]> {
    return this.http.get<Environment[]>('/api/environments');
  }

  login(username: string, password: string, environment: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { username, password, environment });
  }

  me(): Observable<AuthUser> {
    return this.http.get<AuthUser>('/api/auth/me');
  }

  changePassword(newPassword: string): Observable<void> {
    return this.http.put<void>('/api/auth/password', { newPassword });
  }
}
