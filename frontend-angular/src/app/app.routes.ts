import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent) },
  { path: 'change-password', loadComponent: () => import('./features/login/change-password.component').then(m => m.ChangePasswordComponent), canActivate: [authGuard] },
  { path: 'integrations', loadComponent: () => import('./features/integrations/integrations.component').then(m => m.IntegrationsComponent), canActivate: [authGuard] },
  { path: 'integrations/:integrationId/endpoints', loadComponent: () => import('./features/endpoints/endpoints.component').then(m => m.EndpointsComponent), canActivate: [authGuard] },
  { path: 'executions', loadComponent: () => import('./features/executions/executions.component').then(m => m.ExecutionsComponent), canActivate: [adminGuard] },
  { path: 'users', loadComponent: () => import('./features/users/users.component').then(m => m.UsersComponent), canActivate: [adminGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'integrations' },
  { path: '**', redirectTo: 'integrations' },
];
