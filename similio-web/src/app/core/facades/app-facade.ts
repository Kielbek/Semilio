import {effect, inject, Injectable, signal} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';
import {UserService} from '../service/user-service';
import {catchError, filter, finalize, forkJoin, of} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {WebSocketService} from '../service/web-socket-service';

@Injectable({ providedIn: 'root' })
export class AppFacade {
  private router = inject(Router);
  private userService = inject(UserService);
  private wsService = inject(WebSocketService);

  readonly isAppReady = signal(false);
  readonly hideGlobalUI = signal(false);

  constructor() {
    this.setupUiVisibilityListener();

    effect(() => {
      const isAuth = this.userService.isAuthenticated();

      if (isAuth && !this.wsService.isConnected$.value) {
        this.wsService.connect();
      } else if (!isAuth && this.wsService.isConnected$.value) {
        this.wsService.disconnect();
      }
    });
  }

  initialize(): void {
    forkJoin([
      this.userService.fetchUser().pipe(catchError(() => of(null))),
    ]).pipe(
      finalize(() => {
        setTimeout(() => this.isAppReady.set(true), 600);
      })
    ).subscribe();
  }


  private setupUiVisibilityListener(): void {
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      takeUntilDestroyed()
    ).subscribe(() => this.calculateUiVisibility());

    window.addEventListener('resize', () => this.calculateUiVisibility());
  }

  private calculateUiVisibility(): void {
    const isMobile = window.innerWidth < 768;
    const isChatDetail = /^\/chats?(?:\/product)?\/[^/]+$/.test(this.router.url.split('?')[0]);

    this.hideGlobalUI.set(isMobile && isChatDetail);
  }
}
