import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgClass } from '@angular/common';
import { Observable, ReplaySubject, shareReplay, switchMap, take } from 'rxjs';
import { UserService } from '../../../../core/service/user-service';
import { ProductService } from '../../../../core/service/product.service';
import { IUserPublic } from '../../../../core/models/i-user-public';
import { UserProfileHeader } from '../../components/user-profile-header/user-profile-header';
import { UserRatings } from '../../tabs/user-ratings/user-ratings';
import { UserAds } from '../../tabs/user-ads/user-ads';

type TabType = 'ads' | 'ratings';

@Component({
  selector: 'app-user-profile-layout',
  standalone: true,
  imports: [
    UserProfileHeader,
    NgClass,
    UserRatings,
    UserAds
  ],
  templateUrl: './user-profile-layout.html',
  styleUrl: './user-profile-layout.css'
})
export class UserProfileLayout implements OnInit {
  private route = inject(ActivatedRoute);
  private userService = inject(UserService);
  private productService = inject(ProductService);

  user = signal<IUserPublic | null>(null);
  isMine = signal<boolean>(false);
  activeTab = signal<TabType>('ads');

  ratings = signal<any[]>([]);
  isLoadingProfile = signal<boolean>(true);
  isRatingsLoaded = signal<boolean>(false);

  private profileId$ = new ReplaySubject<string>(1);
  private adsCache = new Map<string, Observable<any>>();

  adsSource = (page: number, size: number): Observable<any> => {
    const cacheKey = `${page}-${size}`;

    if (this.adsCache.has(cacheKey)) {
      return this.adsCache.get(cacheKey)!;
    }

    const request$ = this.profileId$.pipe(
      take(1),
      switchMap(id => {
        return this.isMine()
          ? this.productService.getUserProducts(page, size)
          : this.productService.getSellerProducts(id, page, size);
      }),
      shareReplay(1)
    );

    this.adsCache.set(cacheKey, request$);
    return request$;
  };

  ngOnInit() {
    this.loadUserProfile();
  }

  private loadUserProfile() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) return;

    this.isLoadingProfile.set(true);

    this.userService.getUserById(idParam).pipe(take(1)).subscribe({
      next: (user) => {
        this.user.set(user);
        this.initializeContext(user.id);
        this.isLoadingProfile.set(false);
      },
      error: (err) => {
        console.error('Błąd pobierania profilu', err);
        this.isLoadingProfile.set(false);
      }
    });
  }

  private initializeContext(profileId: string) {
    this.userService.getLoggedUserId$().pipe(take(1)).subscribe(loggedId => {
      const isMyProfile = loggedId === profileId;
      this.isMine.set(isMyProfile);

      this.profileId$.next(profileId);
    });
  }

  switchTab(tab: TabType) {
    this.activeTab.set(tab);

    const userId = this.user()?.id;
    if (userId && tab === 'ratings' && !this.isRatingsLoaded()) {
      this.loadRatings(userId);
    }
  }

  private loadRatings(userId: string) {
    this.userService.getUserRatings(userId).pipe(take(1)).subscribe({
      next: (data) => {
        this.ratings.set(data);
        this.isRatingsLoaded.set(true);
      }
    });
  }
}
