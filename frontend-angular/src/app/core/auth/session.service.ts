import { Injectable, computed, signal } from '@angular/core';
import { AuthUser, LoginResponse } from '../models/auth.models';
import { UserRole } from '../models/common.models';

const KEYS = {
  token: 'ihub_token',
  role: 'ihub_role',
  environment: 'ihub_environment',
  environmentName: 'ihub_environment_name',
  passwordChangeRequired: 'ihub_password_change_required',
} as const;

export type SessionValidationStatus = 'validating' | 'authenticated' | 'unauthenticated' | 'unavailable';

@Injectable({ providedIn: 'root' })
export class SessionService {
  readonly token = signal<string | null>(sessionStorage.getItem(KEYS.token));
  readonly role = signal<UserRole | null>(sessionStorage.getItem(KEYS.role) as UserRole | null);
  readonly environment = signal<string | null>(sessionStorage.getItem(KEYS.environment));
  readonly environmentName = signal<string | null>(sessionStorage.getItem(KEYS.environmentName));
  readonly user = signal<AuthUser | null>(null);
  readonly passwordChangeRequired = signal(sessionStorage.getItem(KEYS.passwordChangeRequired) === 'true');
  readonly validationStatus = signal<SessionValidationStatus>(
    sessionStorage.getItem(KEYS.token) ? 'validating' : 'unauthenticated',
  );
  readonly validationError = signal('');
  readonly authenticated = computed(() => this.validationStatus() === 'authenticated');
  readonly canEdit = computed(() => this.role() === 'A' || this.role() === 'C');
  readonly isAdmin = computed(() => this.role() === 'A');

  save(response: LoginResponse, environment: string, environmentName: string): void {
    const role = this.readRole(response.token);
    this.persist(KEYS.token, response.token);
    this.persist(KEYS.role, role ?? '');
    this.persist(KEYS.environment, environment);
    this.persist(KEYS.environmentName, environmentName);
    this.persist(KEYS.passwordChangeRequired, String(response.passwordChangeRequired));

    this.token.set(response.token);
    this.role.set(role);
    this.environment.set(environment);
    this.environmentName.set(environmentName);
    this.passwordChangeRequired.set(response.passwordChangeRequired);
    this.user.set(null);
    this.validationError.set('');
    this.validationStatus.set('validating');
  }

  setUser(user: AuthUser | null): void {
    this.user.set(user);
  }

  setValidationStatus(status: SessionValidationStatus, message = ''): void {
    this.validationStatus.set(status);
    this.validationError.set(message);
  }

  setPasswordChanged(): void {
    this.persist(KEYS.passwordChangeRequired, 'false');
    this.passwordChangeRequired.set(false);
  }

  clear(): void {
    Object.values(KEYS).forEach(key => sessionStorage.removeItem(key));
    this.token.set(null);
    this.role.set(null);
    this.environment.set(null);
    this.environmentName.set(null);
    this.user.set(null);
    this.passwordChangeRequired.set(false);
    this.validationError.set('');
    this.validationStatus.set('unauthenticated');
  }

  private persist(key: string, value: string): void {
    sessionStorage.setItem(key, value);
  }

  private readRole(token: string): UserRole | null {
    try {
      const raw = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = raw.padEnd(Math.ceil(raw.length / 4) * 4, '=');
      return JSON.parse(atob(padded)).role as UserRole;
    } catch {
      return null;
    }
  }
}
