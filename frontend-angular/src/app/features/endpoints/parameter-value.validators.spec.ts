import { FormControl } from '@angular/forms';
import { describe, expect, it } from 'vitest';
import { parameterValueValidator } from './parameter-value.validators';

describe('parameterValueValidator', () => {
  it.each(['12', '-10', '+149.90', '.25', '1.2e3'])('accepts BigDecimal-compatible number %s', value => {
    const validator = parameterValueValidator('NUMBER');
    expect(validator?.(new FormControl(value, { nonNullable: true }))).toBeNull();
  });

  it.each(['12,50', '1 000', 'abc', '1e'])('rejects invalid number %s', value => {
    const validator = parameterValueValidator('NUMBER');
    expect(validator?.(new FormControl(value, { nonNullable: true }))).toEqual({ invalidNumber: true });
  });

  it('allows an empty optional typed value so the required validator can decide whether it is needed', () => {
    expect(parameterValueValidator('NUMBER')?.(new FormControl('', { nonNullable: true }))).toBeNull();
    expect(parameterValueValidator('DATE')?.(new FormControl('', { nonNullable: true }))).toBeNull();
    expect(parameterValueValidator('TIMESTAMP')?.(new FormControl('', { nonNullable: true }))).toBeNull();
  });

  it.each(['2024-02-29', '2026-11-12'])('accepts valid date %s', value => {
    const validator = parameterValueValidator('DATE');
    expect(validator?.(new FormControl(value, { nonNullable: true }))).toBeNull();
  });

  it.each(['2025-02-29', '2024-02-30', '2026-13-01', '12/11/2026'])('rejects invalid date %s', value => {
    const validator = parameterValueValidator('DATE');
    expect(validator?.(new FormControl(value, { nonNullable: true }))).toEqual({ invalidDate: true });
  });

  it('accepts the timestamp format and time range used by the backend', () => {
    const validator = parameterValueValidator('TIMESTAMP');
    expect(validator?.(new FormControl('2024-02-29T23:59:59', { nonNullable: true }))).toBeNull();
  });

  it.each(['2026-11-12 12:30:00', '2026-11-12T24:00:00', '2026-02-29T12:30:00', '2026-11-12T12:30'])('rejects invalid timestamp %s', value => {
    const validator = parameterValueValidator('TIMESTAMP');
    expect(validator?.(new FormControl(value, { nonNullable: true }))).toEqual({ invalidTimestamp: true });
  });

  it('does not add type validation to text parameters', () => {
    expect(parameterValueValidator('VARCHAR2')).toBeNull();
  });
});
