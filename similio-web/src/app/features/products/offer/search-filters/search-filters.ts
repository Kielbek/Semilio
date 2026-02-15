import { Component, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-search-filters',
  imports: [ReactiveFormsModule, FormsModule, NgClass],
  templateUrl: './search-filters.html',
  styleUrl: './search-filters.css',
})
export class SearchFilters implements OnInit {
  filters = {
    category: '',
    brand: '',
    condition: '',
    priceFrom: '',
    priceTo: '',
    sort: '',
  };

  showMoreFilters = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.filters.category = params['category'] || '';
      this.filters.brand = params['brand'] || '';
      this.filters.condition = params['condition'] || '';
      this.filters.priceFrom = params['priceFrom'] || '';
      this.filters.priceTo = params['priceTo'] || '';
      this.filters.sort = params['sort'] || '';
    });
  }

  toggleFilters() {
    this.showMoreFilters = !this.showMoreFilters;
  }

  updateFilters() {
    const queryParams: any = {};

    if (this.filters.category) queryParams.category = this.filters.category;
    if (this.filters.brand) queryParams.brand = this.filters.brand;
    if (this.filters.condition) queryParams.condition = this.filters.condition;
    if (this.filters.priceFrom) queryParams.priceFrom = this.filters.priceFrom;
    if (this.filters.priceTo) queryParams.priceTo = this.filters.priceTo;
    if (this.filters.sort) queryParams.sort = this.filters.sort;

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      queryParamsHandling: 'merge',
    });
  }
}
