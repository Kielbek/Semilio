import { Routes } from '@angular/router';
import {AppConfig} from '../../core/config/app-paths';

const P = AppConfig.SEGMENTS;

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./options.component').then(m => m.Options),
    children: [
      {
        path: P.PROFILE,
        loadComponent: () => import('./components/profile-settings/profile-settings').then(m => m.ProfileSettings)
      },
      {
        path: P.ACCOUNT,
        loadComponent: () => import('./components/account-settings/account-settings').then(m => m.AccountSettings)
      },
      {
        path: P.PRIVACY,
        loadComponent: () => import('./components/privacy-settings/privacy-settings').then(m => m.PrivacySettings)
      },
    ]
  }
];
