import {Component, inject, Input, signal} from '@angular/core';
import {Button} from '../../../../shared/button/button';
import {DatePipe, DecimalPipe} from '@angular/common';
import {Router} from '@angular/router';
import {LucideAngularModule} from 'lucide-angular';
import {IProduct} from '../../../../core/models/product/i-product';
import {AppConfig} from '../../../../core/config/app-paths';
import {ProposePrice} from '../../../chat/components/propose-price/propose-price';
import {AuthService} from '../../../../core/service/auth-service';
import {UserService} from '../../../../core/service/user-service';

@Component({
  selector: 'app-product-info',
  imports: [
    Button,
    DatePipe,
    LucideAngularModule,
    DecimalPipe,
    ProposePrice
  ],
  templateUrl: './product-info.html',
  styleUrl: './product-info.css',
})
export class ProductInfo {
  private router = inject(Router);
  private authService = inject(AuthService)
  private userService = inject(UserService);

  @Input({ required: true }) product?: IProduct;
  @Input() isMine: boolean = false;

  showPriceModal = signal(false);

  linkCopied = false;

  openChat(): void {
    if (!this.ensureAuthenticated()) return;

    if (!this.product || this.isMine) return;

    const contextData = {
      productId: this.product.id,
      sellerId: this.product.seller.id,
      productTitle: this.product.title,
      productPrice: this.product.price,
      productImage: this.product.images[0]
    };

    this.router.navigate([AppConfig.LINKS.CHAT + '/product', this.product.id], {
      state: {
        chatContext: contextData
      }
    });
  }

  openPriceProposal() {
    if (!this.ensureAuthenticated()) return;

    this.showPriceModal.set(true);
  }

  editProduct() {
    if (!this.ensureAuthenticated()) return;

    if (!this.product || !this.isMine) return;

    this.router.navigate([AppConfig.LINKS.PRODUCT.EDIT_ROOT, this.product.id]);
  }

  async shareProduct() {
    const productUrl = `${window.location.origin}/${AppConfig.LINKS.PRODUCT.DETAILS}/${this.product?.slug}`;

    if (navigator.share) {
      try {
        await navigator.share({
          title: this.product?.title,
          text: `Sprawdź ten produkt: ${this.product?.title}`,
          url: productUrl
        });
      } catch (error) {
        console.log('Udostępnianie anulowane');
      }
    } else {
      this.copyToClipboard(productUrl);
    }
  }

  private copyToClipboard(url: string) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(url)
          .then(() => this.showSuccessMessage())
          .catch(() => this.fallbackCopy(url));
      } else {
        this.fallbackCopy(url);
      }
    }

  private fallbackCopy(url: string) {
      try {
        const textArea = document.createElement('textarea');
        textArea.value = url;

        // Zapobiegamy scrollowaniu strony przy dodawaniu elementu
        textArea.style.position = 'fixed';
        textArea.style.left = '-9999px';
        textArea.style.top = '0';

        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        const successful = document.execCommand('copy');
        document.body.removeChild(textArea);

        if (successful) {
          this.showSuccessMessage();
        }
      } catch (err) {
        console.error('Nawet fallback zawiódł:', err);
      }
    }

  private showSuccessMessage() {
      // Tutaj Twój toast lub alert\
    }

  private ensureAuthenticated(): boolean {
    if (this.userService.isAuthenticated()) {
      return true;
    }

    this.authService.openLoginPopup();
    return false;
  }
}
