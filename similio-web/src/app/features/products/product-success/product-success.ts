import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { Button } from '../../../shared/button/button';
import { IProductCard } from '../../../core/models/product/i-product-card';
import { AppConfig } from '../../../core/config/app-paths';

@Component({
  selector: 'app-product-success',
  standalone: true,
  imports: [
    Button,
    DecimalPipe
  ],
  templateUrl: './product-success.html',
  styleUrl: './product-success.css'
})
export class ProductSuccess implements OnInit {
  private readonly router = inject(Router);
  protected readonly product = signal<IProductCard | null>(null);
  protected readonly paths = AppConfig;

  constructor() {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras.state || history.state;

    if (state?.['product']) {
      this.product.set(state['product']);
    }
  }

  ngOnInit(): void {
    if (!this.product()) {
      this.router.navigate([this.paths.LINKS.HOME]);
    }
  }

  addAnother(): void {
    this.router.navigate([this.paths.LINKS.PRODUCT.CREATE]);
  }

  goToProduct(): void {
    const currentProduct = this.product();
    if (currentProduct?.slug) {
      this.router.navigate([this.paths.LINKS.PRODUCT.DETAILS, currentProduct.slug]);
    }
  }

  goToHome(): void {
    this.router.navigate([this.paths.LINKS.HOME]);
  }
}
