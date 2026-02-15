import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  inject,
  ViewChild,
  signal
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { Observable } from 'rxjs';

import { LayoutService } from '../../core/service/layout-service';
import { AuthService } from '../../core/service/auth-service';
import { UserService } from '../../core/service/user-service';
import { IUser } from '../../core/models/i-user';

import { Button } from '../../shared/button/button';

import {
  LucideAngularModule,
  LUCIDE_ICONS,
  LucideIconProvider,
  MessageCircle,
  Heart, Route
} from 'lucide-angular';
import {UserDropdown} from '../../shared/user-dropdown/user-dropdown';
import {AppConfig} from '../../core/config/app-paths';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    RouterLink,
    AsyncPipe,
    Button,
    LucideAngularModule,
    UserDropdown
  ],
  templateUrl: './header.html',
  styleUrls: ['./header.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({ MessageCircle, Heart })
    }
  ]
})
export class Header implements AfterViewInit {
  private layoutService = inject(LayoutService);
  private authService = inject(AuthService);
  private userService = inject(UserService);

  @ViewChild('headerEl') private headerElement!: ElementRef<HTMLDivElement>;

  readonly user$: Observable<IUser | null> = this.userService.user$;
  readonly isHeaderHidden = signal(false);
  readonly paths = AppConfig;

  private lastScrollTop = 0;
  private readonly scrollThreshold = 5;

  ngAfterViewInit(): void {
    this.updateHeaderHeight();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.updateHeaderHeight();
  }

  @HostListener('window:scroll')
  onWindowScroll(): void {
    const currentScroll = window.scrollY || document.documentElement.scrollTop;

    if (Math.abs(currentScroll - this.lastScrollTop) <= this.scrollThreshold) {
      return;
    }

    this.isHeaderHidden.set(currentScroll > this.lastScrollTop);
    this.lastScrollTop = Math.max(currentScroll, 0);
  }

  logout(): void {
    this.authService.logout();
  }

  login(): void {
    this.authService.openLoginPopup();
  }

  private updateHeaderHeight(): void {
    if (this.headerElement?.nativeElement) {
      this.layoutService.setHeaderHeight(this.headerElement.nativeElement.offsetHeight);
    }
  }
}
