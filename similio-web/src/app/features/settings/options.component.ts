import { Component, HostListener, inject, OnInit, signal, computed } from '@angular/core';
import {
  ArrowLeft, ChevronRight, Lock, LogOut, LUCIDE_ICONS,
  LucideAngularModule, LucideIconProvider, Settings, User
} from 'lucide-angular';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../core/service/auth-service';
import { AppConfig } from '../../core/config/app-paths';

@Component({
  selector: 'app-options',
  standalone: true,
  imports: [LucideAngularModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './options.component.html',
  styleUrl: './options.component.css',
  providers: [
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({ ChevronRight, LogOut, ArrowLeft, User, Settings, Lock })
    }
  ],
})
export class Options implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  // 1. Tworzymy sygnał dla adresu URL
  private readonly currentUrl = signal(this.router.url);

  readonly menuItems = [
    { label: 'Szczegóły profilu', path: AppConfig.SEGMENTS.PROFILE, icon: 'user' },
    { label: 'Ustawienia konta', path: AppConfig.SEGMENTS.ACCOUNT, icon: 'settings' },
    { label: 'Ustawienia prywatności', path: AppConfig.SEGMENTS.PRIVACY, icon: 'lock' },
  ];

  readonly windowWidth = signal(window.innerWidth);
  readonly isMobileView = computed(() => this.windowWidth() < 768);

  readonly isChildRouteActive = computed(() => {
    const settingsRoot = `/${AppConfig.SEGMENTS.SETTINGS}`;
    return this.currentUrl() !== settingsRoot && this.currentUrl() !== `${settingsRoot}/`;
  });

  ngOnInit(): void {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.currentUrl.set(this.router.url);
    });

    this.handleInitialRedirect();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.windowWidth.set(window.innerWidth);
    this.handleInitialRedirect();
  }

  private handleInitialRedirect(): void {
    const settingsRoot = `/${AppConfig.SEGMENTS.SETTINGS}`;
    const profilePath = `${settingsRoot}/${AppConfig.SEGMENTS.PROFILE}`;

    if (!this.isMobileView() && this.router.url === settingsRoot) {
      this.router.navigate([profilePath], { replaceUrl: true });
    }
  }

  goBack(): void {
    this.router.navigate([`/${AppConfig.SEGMENTS.SETTINGS}`]);
  }

  logout(): void {
    this.authService.logout();
  }
}
