import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth-guard/auth-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'auth' },
  {
    path: 'auth', 
    loadChildren: () => 
      import('./features/auth/auth.route').then(r => r.AUTH_ROUTES)
  },
  {
    path: 'home',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/home/home.route').then(r => r.HOME_ROUTES),
  },
];
