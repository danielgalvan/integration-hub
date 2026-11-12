import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const BIG_DECIMAL = /^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$/;
const DATE = /^(\d{4})-(\d{2})-(\d{2})$/;
const TIMESTAMP = /^(\d{4}-\d{2}-\d{2})T(\d{2}):(\d{2}):(\d{2})$/;

export function parameterValueValidator(type: string): ValidatorFn | null {
  switch (type.toUpperCase()) {
    case 'NUMBER': return numberValidator;
    case 'DATE': return dateValidator;
    case 'TIMESTAMP': return timestampValidator;
    default: return null;
  }
}

function numberValidator(control: AbstractControl<string>): ValidationErrors | null {
  const value = control.value;
  return !value || BIG_DECIMAL.test(value) ? null : { invalidNumber: true };
}

function dateValidator(control: AbstractControl<string>): ValidationErrors | null {
  const value = control.value;
  return !value || isValidDate(value) ? null : { invalidDate: true };
}

function timestampValidator(control: AbstractControl<string>): ValidationErrors | null {
  const value = control.value;
  if (!value) return null;
  const match = TIMESTAMP.exec(value);
  if (!match || !isValidDate(match[1])) return { invalidTimestamp: true };
  const [, , hours, minutes, seconds] = match;
  return Number(hours) <= 23 && Number(minutes) <= 59 && Number(seconds) <= 59
    ? null
    : { invalidTimestamp: true };
}

function isValidDate(value: string): boolean {
  const match = DATE.exec(value);
  if (!match) return false;
  const [, yearText, monthText, dayText] = match;
  const year = Number(yearText);
  const month = Number(monthText);
  const day = Number(dayText);
  if (month < 1 || month > 12 || day < 1) return false;
  const leapYear = year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0);
  const daysInMonth = [31, leapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
  return day <= daysInMonth[month - 1];
}
