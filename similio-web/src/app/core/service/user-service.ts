import {inject, Injectable} from '@angular/core';
import {IUser} from '../models/i-user';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, catchError, delay, map, Observable, of, tap} from 'rxjs';
import {environment} from '../../../environment';
import {IUserPublic} from '../models/i-user-public';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);

  private readonly baseUrl = environment.apiBase + '/users';
  private readonly TOKEN_KEY = 'access_token';
  private currentUser$ = new BehaviorSubject<IUser | null>(null);


  fetchUser(forceRefresh = false): Observable<IUser | null> {
    const token = localStorage.getItem(this.TOKEN_KEY);

    if (!token) {
      this.currentUser$.next(null);
      return of(null);
    }

    if (!forceRefresh && this.currentUser$.value) {
      return of(this.currentUser$.value);
    }

    return this.http.get<IUser>(`${this.baseUrl}/me`).pipe(
      tap(user => {
        this.currentUser$.next(user);
      }),
      catchError(err => {
        this.currentUser$.next(null);
        console.error('Błąd pobierania użytkownika (możliwe wygaśnięcie sesji)', err);
        return of(null);
      })
    );
  }

  get user$(): Observable<IUser | null> {
    return this.currentUser$.asObservable();
  }

  getLoggedUserId(): string {
    const user = this.currentUser$.value;
    return user ? user.id : '';
  }

  getLoggedUserId$(): Observable<string | null> {
    return this.currentUser$.asObservable().pipe(
      map(user => user ? user.id : null)
    );
  }

  getUser(): IUser | null {
    return this.currentUser$.value;
  }

  clearUser(): void {
    this.currentUser$.next(null);
  }

  updateProfile(request: any, imageFile: File | null): Observable<void> {
    const formData = new FormData();

    const jsonBlob = new Blob([JSON.stringify(request)], { type: 'application/json' });
    formData.append('request', jsonBlob);

    if (imageFile) {
      formData.append('profileImage', imageFile);
    }

    return this.http.patch<void>(`${this.baseUrl}/me`, formData);
  }

  changePassword(currentPassword: string, newPassword: string, confirmNewPassword: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/me/password`, {currentPassword, newPassword, confirmNewPassword});
  }

  getUserById(userId: string): Observable<IUserPublic> {
    return this.http.get<IUserPublic>(`${this.baseUrl}/public/${userId}`);
  }

  getUserRatings(userId: string): Observable<any[]> {
    return of([
    ]).pipe(delay(300));
  }
}
