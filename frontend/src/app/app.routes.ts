import { Routes } from '@angular/router';
import { authGuard } from './shared/guards/auth-guard/auth-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/register/register-page/register-page').then((m) => m.RegisterPage),
  },
  {
    path: 'forgot_password',
    loadComponent: () =>
      import('./features/forgot-password/forgot-password-page/forgot-password-page').then(
        (m) => m.ForgotPasswordPage
      ),
  },
  {
    // Caminho definido pelo link enviado no e-mail (MailService no backend)
    path: 'reset_password',
    loadComponent: () =>
      import('./features/reset-password/reset-password-page/reset-password-page').then(
        (m) => m.ResetPasswordPage
      ),
  },
  {
    // Caminho definido pelo link enviado no e-mail (MailService no backend)
    path: 'validate_user',
    loadComponent: () =>
      import('./features/validate-user/validate-user-page/validate-user-page').then(
        (m) => m.ValidateUserPage
      ),
  },
  {
    path: 'home',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/home/home/home.component').then((m) => m.HomeComponent),
  },
];
