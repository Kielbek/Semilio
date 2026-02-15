import {Routes} from '@angular/router';
import {authGuard} from './core/guards/auth-guard';
import {AppConfig} from './core/config/app-paths';

const P = AppConfig.SEGMENTS;

export let routes: Routes;
routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home-page/home-page').then(m => m.HomePage),
    title: 'Semilio | Kupuj i sprzedawaj w najlepszych cenach',
    pathMatch: 'full'
  },
  {
    path: 'home',
    redirectTo: '',
    pathMatch: 'full'
  },

  {
    path: P.RESET_PASS,
    loadComponent: () => import('./features/auth/pages/reset-password/reset-password').then(m => m.ResetPassword),
    title: 'Zresetuj hasło'
  },
  {
    path: P.SEARCH,
    loadComponent: () => import('./features/products/offer/search-results.component').then(m => m.SearchResults),
    title: 'Szukaj'
  },
  {
    path: `${P.DETAILS}/:slug`,
    loadComponent: () => import('./features/products/details/details').then(m => m.Details)
  },

  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: P.CREATE,
        loadComponent: () => import('./features/products/add/add').then(m => m.Add),
        title: 'Dodaj ogłoszenie'
      },
      {
        path: `${P.EDIT}/:id`,
        loadComponent: () => import('./features/products/add/add').then(m => m.Add),
        title: 'Edytuj ogłoszenie'
      },
      {
        path: P.SUCCESS,
        loadComponent: () => import('./features/products/product-success/product-success').then(m => m.ProductSuccess)
      },
      {
        path: P.FAVORITE,
        loadComponent: () => import('./features/favorite/favorite').then(m => m.Favorite),
        title: 'Ulubione'
      }
    ]
  },

  {
    path: P.CHAT,
    canActivate: [authGuard],
    loadChildren: () => import('./features/chat/chat.routes').then(m => m.CHAT_ROUTES)
  },

  {
    path: P.SETTINGS,
    canActivate: [authGuard],
    loadChildren: () => import('./features/settings/settings.routes').then(m => m.SETTINGS_ROUTES)
  },
  {
    path: `${P.PROFILE}/:id`,
    loadChildren: () => import('./features/user/user.routes').then(m => m.USER_ROUTES)
  },

  {
    path: '',
    loadChildren: () => import('./features/info/info.routes').then(m => m.INFO_ROUTES)
  },

  {
    path: '**',
    loadComponent: () => import('./features/error/not-found/not-found').then(m => m.NotFound),
    title: 'Strona nie znaleziona | Semilio',
  }
];
