import {Component, inject, OnInit} from '@angular/core';
import {LayoutService} from './core/service/layout-service';
import {Footer} from './layout/footer/footer';
import {RouterOutlet} from '@angular/router';
import {Auth} from './features/auth/pages/auth/auth';
import {Header} from './layout/header/header';
import {Toast} from './shared/toast/toast.component';
import {MobileNav} from './layout/mobile-nav/mobile-nav';
import {CommonModule} from '@angular/common';
import {AppFacade} from './core/facades/app-facade';
import {GlobalLoader} from './layout/global-loader/global-loader';
import {UserService} from './core/service/user-service';
import {delay} from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [Header, Footer, RouterOutlet, Auth, Toast, MobileNav, CommonModule, GlobalLoader],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  public facade = inject(AppFacade);
  public layoutService = inject(LayoutService);
  public userService = inject(UserService);

  readonly headerHeightDelayed$ = this.layoutService.headerHeight$.pipe(delay(0));

  ngOnInit(): void {
    this.facade.initialize();
  }
}
