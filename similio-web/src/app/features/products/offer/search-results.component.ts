import {Component, inject, OnInit, signal, ViewChild} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {SearchBar} from '../../../shared/search-bar/search-bar';
import {SearchFilters} from './search-filters/search-filters';
import {ActivatedRoute} from '@angular/router';
import {ProductService} from '../../../core/service/product.service';
import {Card} from '../components/card/card';
import {List} from '../list/list';
import {FetchFunction} from '../../../core/types/fetch-function';
import {IProductSearchCriteria} from '../../../core/models/i-product-search-criteria';
import {EmptyState} from '../components/empty-state/empty-state';

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

  currentQuery = signal<string>('');

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const queryFromUrl = params['query'] || '';

      if (this.currentQuery() !== queryFromUrl) {
        this.currentQuery.set(queryFromUrl);

        if (this.listComponent) {
          this.listComponent.refresh();
        }
      }
    });
  }

  fetchProducts: FetchFunction = (page: number) => {
    const criteria: IProductSearchCriteria = {
      query: this.currentQuery()
    };

    return this.productService.search(criteria, page);
  };
}
