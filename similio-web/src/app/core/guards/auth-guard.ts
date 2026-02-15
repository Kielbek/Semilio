import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from '../service/auth-service';
import {UserService} from '../service/user-service';
import {catchError, from, map, of, switchMap, take} from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const userService = inject(UserService);
  const router = inject(Router);

  return userService.user$.pipe(
    take(1),
    switchMap(user => user ? of(true) : from(userService.fetchUser(true)).pipe(
      map(fetchedUser => !!fetchedUser),
      catchError(() => {
        router.navigate(['/home']);
        take(1)
        auth.openLoginPopup();
        return of(false);
      })
    ))
  );
};
