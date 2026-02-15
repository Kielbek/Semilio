import { Routes } from '@angular/router';
import {AppConfig} from '../../core/config/app-paths';

const P = AppConfig.SEGMENTS;

export const INFO_ROUTES: Routes = [
  {
    path: P.ABOUT,
    children: [
      {
        path: P.PLATFORM,
        loadComponent: () => import('./about/platform/platform').then(m => m.Platform),
        title: 'Nasza Platforma | Semilio'
      },
      {
        path: P.SUSTAINABILITY,
        loadComponent: () => import('./about/sustainability/sustainability').then(m => m.Sustainability),
        title: 'Zrównoważony Rozwój | Semilio'
      },
      {
        path: P.PRESS,
        loadComponent: () => import('./about/press/press').then(m => m.Press),
        title: 'Dla Prasy | Semilio'
      }
    ]
  },

  {
    path: P.HELP,
    loadComponent: () => import('./help/center/center').then(m => m.Center),
    children: [
      {
        path: P.SELLING,
        loadComponent: () => import('./help/selling/selling').then(m => m.Selling),
        title: 'Jak sprzedawać? | Semilio'
      },
      {
        path: P.BUYING,
        loadComponent: () => import('./help/buying/buying').then(m => m.Buying),
        title: 'Jak kupować? | Semilio'
      },
      {
        path: P.SAFETY,
        loadComponent: () => import('./help/safety/safety').then(m => m.Safety),
        title: 'Bezpieczeństwo | Semilio'
      }
    ]
  },

  {
    path: P.LEGAL,
    children: [
      {
        path: P.TERMS,
        loadComponent: () => import('./legal/terms/terms').then(m => m.Terms),
        title: 'Regulamin | Semilio'
      },
      {
        path: P.COOKIES,
        loadComponent: () => import('./legal/cookies/cookies').then(m => m.Cookies),
        title: 'Polityka Cookies | Semilio'
      },
      {
        path: P.PRIVACY,
        loadComponent: () => import('./legal/privacy/privacy').then(m => m.Privacy),
        title: 'Polityka Prywatności | Semilio'
      }
    ]
  }
];
