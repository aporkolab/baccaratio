import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

/** Sends the JWT to our API only, and returns to the login page when the API rejects it. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();
  const apiBase = environment.apiUrl.replace(/\/+$/, '');
  const isApiUrl = !!apiBase && (req.url === apiBase
    || req.url.startsWith(`${apiBase}/`)
    || req.url.startsWith(`${apiBase}?`));
  const isAuthRequest = isApiUrl && req.url.startsWith(`${apiBase}/auth/`);

  if (token && isApiUrl && !isAuthRequest) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && isApiUrl && !isAuthRequest) {
        authService.logout();
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
