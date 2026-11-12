import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take } from 'rxjs';
import { SessionService } from './session.service';
export const adminGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);
  return toObservable(session.validationStatus).pipe(
    filter(status => status !== 'validating'),
    take(1),
    map(status => {
      if (status === 'unavailable') return false;
      if (status !== 'authenticated') return router.createUrlTree(['/login']);
      if (session.passwordChangeRequired()) return router.createUrlTree(['/change-password']);
      return session.isAdmin() ? true : router.createUrlTree(['/integrations']);
    }),
  );
};
