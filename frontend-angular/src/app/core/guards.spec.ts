import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, GuardResult, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { firstValueFrom, isObservable } from 'rxjs';
import { beforeEach, describe, expect, it } from 'vitest';
import { adminGuard } from './admin.guard';
import { authGuard } from './auth.guard';
import { SessionService } from './session.service';

const tokenFor = (role: string): string => `header.${btoa(JSON.stringify({ role }))}.signature`;
const route = (path: string) => ({ routeConfig: { path } }) as ActivatedRouteSnapshot;
const state = {} as RouterStateSnapshot;

describe('route guards', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  async function decide(result: ReturnType<typeof authGuard> | ReturnType<typeof adminGuard>): Promise<GuardResult> {
    return isObservable(result) ? firstValueFrom(result) : await result;
  }

  it('redirects unauthenticated users to login', async () => {
    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => authGuard(route('integrations'), state));

    const decision = await decide(result);
    expect(decision).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(decision as UrlTree)).toBe('/login');
  });

  it('waits for initial session validation before activating a protected route', async () => {
    const session = TestBed.inject(SessionService);
    session.save({ token: tokenFor('A'), tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false }, 'cloud', 'Cloud');
    const result = TestBed.runInInjectionContext(() => authGuard(route('integrations'), state));
    let resolved = false;
    const decision = decide(result).then(value => { resolved = true; return value; });

    await new Promise(resolve => setTimeout(resolve, 0));
    expect(resolved).toBe(false);
    session.setValidationStatus('authenticated');

    expect(await decision).toBe(true);
  });

  it('routes a validated user with a required password change to the password page', async () => {
    const session = TestBed.inject(SessionService);
    session.save({ token: tokenFor('U'), tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: true }, 'cloud', 'Cloud');
    session.setValidationStatus('authenticated');
    const result = TestBed.runInInjectionContext(() => authGuard(route('integrations'), state));

    const decision = await decide(result);
    expect(decision).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(decision as UrlTree)).toBe('/change-password');
  });

  it('allows administration only for a validated administrator', async () => {
    const session = TestBed.inject(SessionService);
    session.save({ token: tokenFor('A'), tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false }, 'cloud', 'Cloud');
    const result = TestBed.runInInjectionContext(() => adminGuard(route('users'), state));
    session.setValidationStatus('authenticated');

    expect(await decide(result)).toBe(true);
  });

  it('sends a non-admin user back to integrations and blocks when validation is unavailable', async () => {
    const session = TestBed.inject(SessionService);
    session.save({ token: tokenFor('C'), tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false }, 'cloud', 'Cloud');
    session.setValidationStatus('authenticated');
    const adminResult = TestBed.runInInjectionContext(() => adminGuard(route('users'), state));
    const nonAdminDecision = await decide(adminResult);
    expect(TestBed.inject(Router).serializeUrl(nonAdminDecision as UrlTree)).toBe('/integrations');

    session.setValidationStatus('unavailable');
    const authResult = TestBed.runInInjectionContext(() => authGuard(route('integrations'), state));
    expect(await decide(authResult)).toBe(false);
  });
});
