import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';

export const authGuard: CanActivateFn = () =>
  inject(AuthService).getAccessToken() ? true : inject(Router).createUrlTree(['/login']);
