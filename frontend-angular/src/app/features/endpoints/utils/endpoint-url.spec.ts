import { describe, expect, it } from 'vitest';
import { buildEndpointUrl } from './endpoint-url';

describe('buildEndpointUrl', () => {
  it('normalizes slashes on the base path and endpoint path', () => {
    expect(buildEndpointUrl('/api/orders/', '/search', {})).toBe('/api/orders/search');
    expect(buildEndpointUrl('/api/orders', 'search', {})).toBe('/api/orders/search');
  });

  it('omits empty, null, and undefined query values', () => {
    expect(buildEndpointUrl('/api/orders', '/search', {
      empty: '',
      missing: null,
      absent: undefined,
      zero: '0',
    })).toBe('/api/orders/search?zero=0');
  });

  it('encodes parameter names and reserved characters in values', () => {
    expect(buildEndpointUrl('/api/orders', '/search', {
      'order id': 'A & B/50%',
    })).toBe('/api/orders/search?order+id=A+%26+B%2F50%25');
  });

  it('preserves date, timestamp, and decimal parameter text', () => {
    expect(buildEndpointUrl('/api/orders', '/search', {
      createdAfter: '2026-11-13',
      createdBefore: '2026-11-13T10:30:00',
      minimumTotal: '149.90',
    })).toBe('/api/orders/search?createdAfter=2026-11-13&createdBefore=2026-11-13T10%3A30%3A00&minimumTotal=149.90');
  });
});
