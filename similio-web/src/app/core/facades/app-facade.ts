import { inject, Injectable, signal } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { UserService } from '../service/user-service';
import { filter, finalize, forkJoin, tap, catchError, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class AppFacade {
  private router = inject(Router);
  private userService = inject(UserService);

  readonly isAppReady = signal(false);
  readonly hideGlobalUI = signal(false);

  constructor() {
    this.setupUiVisibilityListener();
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
    // Regex na stronę czatu
    const isChatDetail = /^\/chats?(?:\/product)?\/[^/]+$/.test(this.router.url.split('?')[0]);

    this.hideGlobalUI.set(isMobile && isChatDetail);
  }
}
