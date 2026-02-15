import { Routes } from '@angular/router';

export const CHAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/chat-layout/chat-layout').then(m => m.ChatLayout),
    children: [
      {
        path: ':id',
        loadComponent: () => import('./components/chat-detail/chat-detail').then(m => m.ChatDetail)
      },
      {
        path: `product/:productId`,
        loadComponent: () => import('./components/chat-detail/chat-detail').then(m => m.ChatDetail)
      }
    ]
  }
];
