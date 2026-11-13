import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { adminGuard } from './core/auth/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/pages/login.component').then(module => module.LoginComponent),
  },
  {
    path: 'change-password',
    loadComponent: () => import('./features/login/pages/change-password.component').then(module => module.ChangePasswordComponent),
    canActivate: [authGuard],
  },
  {
    path: 'integrations',
    loadComponent: () => import('./features/integrations/pages/integrations.component').then(module => module.IntegrationsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'integrations/:integrationId/endpoints',
    loadComponent: () => import('./features/endpoints/pages/endpoints.component').then(module => module.EndpointsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'executions',
    loadComponent: () => import('./features/executions/pages/executions.component').then(module => module.ExecutionsComponent),
    canActivate: [adminGuard],
  },
  {
    path: 'users',
    loadComponent: () => import('./features/users/pages/users.component').then(module => module.UsersComponent),
    canActivate: [adminGuard],
  },
  { path: '', pathMatch: 'full', redirectTo: 'integrations' },
  { path: '**', redirectTo: 'integrations' },
];
