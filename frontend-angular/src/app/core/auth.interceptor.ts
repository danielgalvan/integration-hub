import { HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionService } from './session.service';

export const SKIP_AUTH_REDIRECT = new HttpContextToken<boolean>(() => false);

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(SessionService); const router = inject(Router); const token = session.token();
  const authenticated = token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;
  return next(authenticated).pipe(catchError(error => {
    if (error.status === 401 && !request.url.endsWith('/api/auth/login') && !request.context.get(SKIP_AUTH_REDIRECT)) { session.clear(); void router.navigateByUrl('/login'); }
    return throwError(() => error);
  }));
};
