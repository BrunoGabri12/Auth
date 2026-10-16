import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../services/auth/auth.service';

const PUBLIC_ROUTES: { method: string; path: string }[] = [
  { method: 'POST', path: '/auth/login' },
  { method: 'POST', path: '/auth/login/google' },
  { method: 'POST', path: '/user/register' },
];

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl) || isPublicAuthRequest(req)) {
    return next(req);
  }

  const authService = inject(AuthService);
  const accessToken = authService.getAccessToken();

  return next(withToken(req, accessToken)).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401 || !accessToken) {
        return throwError(() => error);
      }

      return authService.refresh().pipe(
        catchError((refreshError) => {
          authService.logout();
          return throwError(() => refreshError);
        }),
        switchMap((tokens) => next(withToken(req, tokens.accessToken)))
      );
    })
  );
};

function withToken(req: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  return token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
}

function isPublicAuthRequest(req: HttpRequest<unknown>): boolean {
  const path = req.url.slice(environment.apiUrl.length).split('?')[0].replace(/\/+$/, '');
  return PUBLIC_ROUTES.some((route) => route.method === req.method && route.path === path);
}
