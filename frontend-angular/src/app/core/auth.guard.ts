import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take } from 'rxjs';
import { SessionService } from './session.service';
export const authGuard: CanActivateFn = route => {
  const session = inject(SessionService);
  const router = inject(Router);
  const isChangingPassword = route.routeConfig?.path === 'change-password';
  return toObservable(session.validationStatus).pipe(
    filter(status => status !== 'validating'),
    take(1),
    map(status => {
      if (status === 'unavailable') return false;
      if (status !== 'authenticated') return router.createUrlTree(['/login']);
      if (session.passwordChangeRequired() && !isChangingPassword) return router.createUrlTree(['/change-password']);
      if (!session.passwordChangeRequired() && isChangingPassword) return router.createUrlTree(['/integrations']);
      return true;
    }),
  );
};
