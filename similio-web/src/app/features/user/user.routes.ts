import { Routes } from '@angular/router';

export const USER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/user-profile-layout/user-profile-layout').then(m => m.UserProfileLayout),
  }
];
