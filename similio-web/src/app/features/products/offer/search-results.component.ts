import { Component, inject, OnInit, signal, computed, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { tap } from 'rxjs/operators';
import { SearchBar } from '../../../shared/search-bar/search-bar';
import { SearchFilters } from './search-filters/search-filters';
import { ProductService } from '../../../core/service/product.service';
import { Card } from '../components/card/card';
import { List } from '../list/list';
import { FetchFunction } from '../../../core/types/fetch-function';
import { IProductSearchCriteria } from '../../../core/models/i-product-search-criteria';
import { EmptyState } from '../components/empty-state/empty-state';

@Component({
  selector: 'app-offer',
  imports: [FormsModule, SearchBar, SearchFilters, Card, List, EmptyState],
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css',
})
export class SearchResults implements OnInit {
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);

  @ViewChild(List) listComponent!: List;

  // Trzymamy CAŁY obiekt z filtrami, a nie tylko wyszukiwane słowo
  currentCriteria = signal<IProductSearchCriteria>({});
  currentQuery = signal<string>('');

  totalResults = signal<number | null>(null);
  showShippingInfo = signal<boolean>(true);

  resultsText = computed(() => {
    const count = this.totalResults();
    if (count === null) return '';

    if (count > 1000) return 'Znaleźliśmy dla Ciebie ponad 1000 ogłoszeń.';

    if (count === 1) return 'Znaleźliśmy dla Ciebie 1 ogłoszenie.';

    const lastDigit = count % 10;
    const lastTwoDigits = count % 100;

    if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 12 || lastTwoDigits > 14)) {
      return `Znaleźliśmy dla Ciebie ${count} ogłoszenia.`;
    }

    return `Znaleźliśmy dla Ciebie ${count} ogłoszeń.`;
  });

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const newCriteria: IProductSearchCriteria = {
        query: params['query'] || undefined,
        minPrice: params['priceFrom'] ? Number(params['priceFrom']) : undefined,
        maxPrice: params['priceTo'] ? Number(params['priceTo']) : undefined,
        condition: params['condition'] || undefined,

        sizeId: params['size'] ? Number(params['size']) : undefined,
        brandId: params['brand'] ? Number(params['brand']) : undefined,
        colorId: params['color'] ? Number(params['color']) : undefined,
        categoryId: params['category'] ? Number(params['category']) : undefined,

        sort: params['sort'] || undefined,
      };

      this.currentCriteria.set(newCriteria);
      this.currentQuery.set(params['query'] || '');

      if (this.listComponent) {
        this.listComponent.refresh();
      }
    });
  }

  closeShippingInfo() {
    this.showShippingInfo.set(false);
  }

  fetchProducts: FetchFunction = (page: number) => {
    return this.productService.search(this.currentCriteria(), page).pipe(
      tap(response => {
        this.totalResults.set(response.page.totalElements);
      })
    );
  };
}
