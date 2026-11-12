import { TestBed } from '@angular/core/testing';
import { SessionService } from './session.service';

describe('SessionService', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [SessionService] });
  });

  it('persists the role from a JWT and exposes permissions', () => {
    const payload = btoa(JSON.stringify({ role: 'A' }));
    const token = `header.${payload}.signature`;
    const service = TestBed.inject(SessionService);

    service.save({ token, tokenType: 'Bearer', expiresIn: 3600, passwordChangeRequired: false }, 'cloud', 'Cloud');

    expect(service.authenticated()).toBeTrue();
    expect(service.role()).toBe('A');
    expect(service.isAdmin()).toBeTrue();
    expect(service.canEdit()).toBeTrue();
    expect(sessionStorage.getItem('ihub_environment')).toBe('cloud');
  });

  it('clears the session and password-change state', () => {
    const service = TestBed.inject(SessionService);
    service.clear();

    expect(service.authenticated()).toBeFalse();
    expect(service.role()).toBeNull();
    expect(service.passwordChangeRequired()).toBeFalse();
    expect(sessionStorage.length).toBe(0);
  });
});
