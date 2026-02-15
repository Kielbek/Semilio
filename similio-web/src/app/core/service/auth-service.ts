import {inject, Injectable} from '@angular/core';
import {BehaviorSubject, Observable, tap, throwError} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environment';
import {IAuthResponse} from '../models/i-auth-response';
import {UserService} from './user-service';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private router = inject(Router);
  private http = inject(HttpClient);
  private userService = inject(UserService);

  showLoginPopup$ = new BehaviorSubject<boolean>(false);
  private apiUrl = environment.apiBase + '/auth';
  private accessTokenKey = 'access_token';
  private refreshTokenKey = 'refresh_token';
  public isLoggedIn$ = new BehaviorSubject<boolean>(!!this.getAccessToken());

  login(email: string, password: string): Observable<IAuthResponse> {
    return this.http.post<IAuthResponse>(`${this.apiUrl}/login`, { email, password })
      .pipe(
        tap(res => {
          this.setTokens(res.access_token, res.refresh_token);
          this.userService.fetchUser().subscribe();
          this.closeLoginPopup();
          this.isLoggedIn$.next(true);
        })
      );
  }

  register(firstName: string, lastName: string, email: string, phoneNumber: string, password: string, confirmPassword: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/register`, { firstName, lastName, email, phoneNumber, password, confirmPassword });
  }

  refreshToken(): Observable<IAuthResponse> {
    const refresh = this.getRefreshToken();
    if (!refresh) return throwError(() => new Error('No refresh token'));
    return this.http.post<IAuthResponse>(`${this.apiUrl}/refresh`, { refreshToken: refresh })
      .pipe(
        tap(res => {
          this.setTokens(res.access_token, res.refresh_token || refresh);
        })
      );
  }

  verifySms(code: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/verify-sms`, { code });
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, password: string, confirmPassword: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reset-password`, { token, password, confirmPassword });
  }

  logout() {
    localStorage.removeItem(this.accessTokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    this.isLoggedIn$.next(false)
    this.userService.clearUser();
    this.router.navigate(['/home']);
  }

  private setTokens(accessToken: string, refreshToken: string) {
    localStorage.setItem(this.accessTokenKey, accessToken);
    if (refreshToken) localStorage.setItem(this.refreshTokenKey, refreshToken);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.accessTokenKey);
  }
  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  openLoginPopup() {
    this.showLoginPopup$.next(true);
  }

  closeLoginPopup() {
    this.showLoginPopup$.next(false);
  }
}
