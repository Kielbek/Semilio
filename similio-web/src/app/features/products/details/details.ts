import {Component, inject, OnInit, signal} from '@angular/core';
import {ProductGallery} from './product-gallery/product-gallery';
import {Title} from '@angular/platform-browser';
import {ProductDescription} from './product-description/product-description';
import {Breadcrumbs} from '../../../shared/breadcrumbs/breadcrumbs';
import {ProductInfo} from './product-info/product-info';
import {SellerCard} from './seller-card/seller-card';
import {ProductService} from '../../../core/service/product.service';
import {ActivatedRoute} from '@angular/router';
import {ViewTrackerService} from '../../../core/service/view-tracker-service';
import {UserService} from '../../../core/service/user-service';
import {IProduct} from '../../../core/models/product/i-product';
import {ReportModal} from '../../../shared/report-modal/report-modal';
import {AuthService} from '../../../core/service/auth-service';

@Component({
  selector: 'app-details',
  imports: [
    ProductGallery,
    ProductDescription,
    Breadcrumbs,
    ProductInfo,
    SellerCard,
    ReportModal,

  ],
  templateUrl: './details.html',
  styleUrl: './details.css',
})
export class Details implements OnInit {
  private viewTracker = inject(ViewTrackerService);
  private titleService = inject(Title);
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private userService = inject(UserService);
  private authService = inject(AuthService)

  product?: IProduct;
  isMine = signal(false);
  isReportModuleOpen = signal(false)

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');

    if (slug == null) return;

    this.productService.getProductBySlug(slug).subscribe({
      next: (data) => {
        this.product = data;
        this.titleService.setTitle(this.product?.title + ' | Semilio');

        this.isMine.set(data.seller.id === this.userService.getLoggedUserId());
        const canTrack = this.viewTracker.canTrackView(data.id);

        if (!this.isMine() && canTrack) {
          this.productService.incrementView(data.id).subscribe();
        }

      },
      error: (err) => {
        console.error('Błąd pobierania produktu', err);
      }
    });
  }

  showReportModule() {
    if (!this.userService.isAuthenticated()) {
      this.authService.openLoginPopup();
      return;
    }

    this.isReportModuleOpen.set(true);
  }
}
