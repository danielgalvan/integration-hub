import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session.service';
export const authGuard: CanActivateFn = (route) => {
  const session = inject(SessionService);
  const router = inject(Router);
  const isChangingPassword = route.routeConfig?.path === 'change-password';

  if (!session.authenticated()) return router.createUrlTree(['/login']);
  if (session.passwordChangeRequired() && !isChangingPassword) return router.createUrlTree(['/change-password']);
  if (!session.passwordChangeRequired() && isChangingPassword) return router.createUrlTree(['/integrations']);
  return true;
};
