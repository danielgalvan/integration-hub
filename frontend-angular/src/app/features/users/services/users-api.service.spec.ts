import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { User } from '../models/user.models';
import { UsersApiService } from './users-api.service';

const user: User = {
  id: 4,
  username: 'criador',
  name: 'Criador de APIs',
  status: 'A',
  type: 'C',
  passwordChangeRequired: false,
};

describe('UsersApiService', () => {
  let api: UsersApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(UsersApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads users', () => {
    api.users().subscribe();
    const request = http.expectOne('/api/users');
    expect(request.request.method).toBe('GET');
    request.flush([user]);
  });

  it('creates a user and returns the temporary password response', () => {
    const value = { username: 'criador', name: 'Criador de APIs', type: 'C' as const };
    api.createUser(value).subscribe();
    const request = http.expectOne('/api/users');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(value);
    request.flush({ user, temporaryPassword: 'temporary' });
  });

  it('updates a user', () => {
    const value = { name: 'Nome atualizado' };
    api.updateUser(user.id, value).subscribe();
    const request = http.expectOne(`/api/users/${user.id}`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(value);
    request.flush({ ...user, ...value });
  });

  it('deletes a user', () => {
    api.deleteUser(user.id).subscribe();
    const request = http.expectOne(`/api/users/${user.id}`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('requests a temporary password reset', () => {
    api.resetPassword(user.id).subscribe();
    const request = http.expectOne(`/api/users/${user.id}/reset-password`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush({ temporaryPassword: 'temporary' });
  });
});
