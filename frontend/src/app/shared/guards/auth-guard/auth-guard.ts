import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../../features/auth/shared/services/auth/auth.service';

export const authGuard: CanActivateFn = () =>
  inject(AuthService).getAccessToken() ? true : inject(Router).createUrlTree(['/auth/login']);
