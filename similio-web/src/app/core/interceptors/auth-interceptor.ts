import { HttpErrorResponse, HttpEvent, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../service/auth-service';
import { BehaviorSubject, catchError, filter, Observable, switchMap, take, throwError } from 'rxjs';

const isRefreshing = { value: false };
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const accessToken = authService.getAccessToken();

  // Pomijamy dodawanie tokena i obsługę 401 dla samego żądania odświeżania
  if (req.url.includes('/auth/refresh')) {
    return next(req);
  }

  const authReq = accessToken
    ? req.clone({
      setHeaders: { Authorization: `Bearer ${accessToken}` },
      withCredentials: true
    })
    : req.clone({ withCredentials: true });

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        return handle401Error(authReq, next, authService);
      }
      return throwError(() => err);
    })
  );
};

function handle401Error(
  req: HttpRequest<any>,
  next: (req: HttpRequest<any>) => Observable<HttpEvent<any>>,
  authService: AuthService
): Observable<HttpEvent<any>> {
  if (!isRefreshing.value) {
    isRefreshing.value = true;
    refreshTokenSubject.next(null);

    return authService.refreshToken().pipe(
      switchMap(newToken => {
        const token = newToken.access_token;
        isRefreshing.value = false;
        refreshTokenSubject.next(token);

        return next(
          req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
          })
        );
      }),
      catchError(err => {
        isRefreshing.value = false;
        authService.logout();
        return throwError(() => err);
      })
    );
  } else {
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(token => {
        return next(req.clone({
          setHeaders: { Authorization: `Bearer ${token}` }
        }));
      })
    );
  }
}
