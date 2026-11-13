import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { SessionService } from './session.service';

const tokenFor = (role: string): string => `header.${btoa(JSON.stringify({ role }))}.signature`;

describe('SessionService', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [SessionService] });
  });

  it('persists the role from a JWT and exposes permissions', () => {
    const service = TestBed.inject(SessionService);

    service.save({ token: tokenFor('A'), tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false }, 'cloud', 'Cloud');

    expect(service.authenticated()).toBe(false);
    expect(service.role()).toBe('A');
    expect(service.isAdmin()).toBe(true);
    expect(service.canEdit()).toBe(true);
    expect(service.environment()).toBe('cloud');
    expect(service.environmentName()).toBe('Cloud');
    expect(service.validationStatus()).toBe('validating');
    expect(sessionStorage.getItem('ihub_environment')).toBe('cloud');
    service.setValidationStatus('authenticated');
    expect(service.authenticated()).toBe(true);
  });

  it('starts in validating state when a token was persisted between page loads', () => {
    sessionStorage.setItem('ihub_token', tokenFor('U'));
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [SessionService] });

    const service = TestBed.inject(SessionService);
    expect(service.authenticated()).toBe(false);
    expect(service.validationStatus()).toBe('validating');
    expect(service.user()).toBeNull();
  });

  it('retains state across a recoverable validation failure and clears it on logout', () => {
    const service = TestBed.inject(SessionService);
    service.save({ token: tokenFor('C'), tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: true }, 'dev', 'Development');
    service.setValidationStatus('unavailable', 'Backend indisponível');

    expect(service.token()).not.toBeNull();
    expect(service.validationStatus()).toBe('unavailable');
    expect(service.validationError()).toBe('Backend indisponível');
    expect(service.passwordChangeRequired()).toBe(true);

    service.clear();
    expect(service.authenticated()).toBe(false);
    expect(service.role()).toBeNull();
    expect(service.passwordChangeRequired()).toBe(false);
    expect(service.validationStatus()).toBe('unauthenticated');
    expect(service.validationError()).toBe('');
    expect(sessionStorage.length).toBe(0);
  });
});
