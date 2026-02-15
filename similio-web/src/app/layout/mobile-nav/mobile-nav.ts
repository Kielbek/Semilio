import {Component, inject, OnInit} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {NgClass} from '@angular/common';
import {
  LucideAngularModule,
  LUCIDE_ICONS,
  LucideIconProvider,
  Home,
  Heart,
  PlusCircle,
  MessageCircle,
  User
} from 'lucide-angular';
import {UserService} from '../../core/service/user-service';
import {AppConfig} from '../../core/config/app-paths';

@Component({
  selector: 'app-mobile-nav',
  imports: [
    RouterLink,
    RouterLinkActive,
    NgClass,
    LucideAngularModule
  ],
  providers: [
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({
        Home,
        Heart,
        PlusCircle,
        MessageCircle,
        User
      })
    }
  ],
  templateUrl: './mobile-nav.html',
  styleUrl: './mobile-nav.css'
})
export class MobileNav implements OnInit {
  private userService = inject(UserService);

  navItems: any[] = [];

  ngOnInit() {
    this.userService.getLoggedUserId$().subscribe(id => {
      const currentId = id || 'me';

      this.navItems = [
        {
          label: 'Start',
          path: AppConfig.LINKS.HOME,
          icon: 'home' },
        {
          label: 'Ulubione',
          path: AppConfig.LINKS.PRODUCT.FAVORITE,
          icon: 'heart' },
        {
          label: 'Sprzedaj',
          path: AppConfig.LINKS.PRODUCT.CREATE,
          icon: 'plus-circle',
          isMainAction: true
        },
        { label: 'Wiadomości', path: AppConfig.LINKS.CHAT, icon: 'message-circle' },
        {
          label: 'Profil',
          path: `/${AppConfig.SEGMENTS.PROFILE}/${currentId}`,
          icon: 'user'
        }
      ];
    });
  }
}
