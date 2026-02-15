import {Component, inject} from '@angular/core';
import {SearchBar} from '../../../shared/search-bar/search-bar';
import {List} from '../../products/list/list';
import {ProductService} from '../../../core/service/product.service';
import {Card} from '../../products/components/card/card';

@Component({
  selector: 'app-home-page',
  imports: [SearchBar, List, Card],
  templateUrl: './home-page.html',
  styleUrl: './home-page.css',
})
export class HomePage {
  private productService = inject(ProductService);

  private readonly sessionSeed = this.generateSeed();

  featuredSource = (page: number, size: number) => {
    return this.productService.getFeaturedProducts(page, size, this.sessionSeed);
  };

  private generateSeed(): string {
    return Math.random().toString(36).substring(2, 15);
  }
}
