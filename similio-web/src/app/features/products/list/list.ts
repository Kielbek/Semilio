import { Component, ElementRef, inject, Input, OnDestroy, OnInit, signal, TemplateRef, ViewChild, AfterViewInit } from '@angular/core';
import { IProductCard } from '../../../core/models/product/i-product-card';
import { finalize } from 'rxjs';
import { FetchFunction } from '../../../core/types/fetch-function';
import { NgTemplateOutlet } from '@angular/common';
import { ListStateService } from '../../../core/service/list-state-service';

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [NgTemplateOutlet],
  templateUrl: './list.html',
  styleUrl: './list.css',
})
export class List implements OnInit, AfterViewInit, OnDestroy {
  private listStateService = inject(ListStateService);

  @Input({ required: true }) fetchDataFn!: FetchFunction;
  @Input({ required: true }) itemTemplate!: TemplateRef<any>;

  private _cacheKey?: string;
  @Input() set cacheKey(value: string | undefined) {
    const previousKey = this._cacheKey;
    this._cacheKey = value;

    if (value && previousKey !== value && !value.includes('null')) {
      if (this.products().length === 0) {
        this.restoreState();
      }
    }
  }
  get cacheKey() { return this._cacheKey; }

  products = signal<IProductCard[]>([]);
  loading = signal(false);

  public currentPage = 0;
  public isLastPage = false;
  private readonly pageSize = 12;
  private currentScrollY = 0;

  @ViewChild('sentinel') sentinel!: ElementRef;
  private observer: IntersectionObserver | null = null;

  ngOnInit() {
    window.addEventListener('scroll', this.onWindowScroll, { passive: true });

    if (this.cacheKey && !this.cacheKey.includes('null')) {
      this.restoreState();
    }

    if (this.products().length === 0) {
      this.loadData();
    }
  }

  ngAfterViewInit() {
    this.setupObserver();
  }

  ngOnDestroy() {
    window.removeEventListener('scroll', this.onWindowScroll);
    this.observer?.disconnect();

    if (this.cacheKey && !this.cacheKey.includes('null') && this.products().length > 0) {
      this.listStateService.saveState(this.cacheKey, {
        products: this.products(),
        currentPage: this.currentPage,
        isLastPage: this.isLastPage,
        scrollPosition: this.currentScrollY
      });
    }
  }

  private onWindowScroll = () => {
    this.currentScrollY = window.scrollY;
  };

  private restoreState(): boolean {
    const state = this.listStateService.getState(this.cacheKey!);
    if (!state) return false;

    this.products.set(state.products);
    this.currentPage = state.currentPage;
    this.isLastPage = state.isLastPage;

    setTimeout(() => {
      window.scrollTo({
        top: state.scrollPosition,
        behavior: 'instant' as any
      });
      window.scroll(0, state.scrollPosition);
    }, 0);

    return true;
  }

  refresh() {
    if (this.cacheKey) {
      this.listStateService.clearState(this.cacheKey);
    }
    this.products.set([]);
    this.currentPage = 0;
    this.isLastPage = false;
    this.loadData();

    // Po odświeżeniu musimy ponownie podpiąć obserwatora
    setTimeout(() => this.setupObserver(), 100);
  }

  removeProductById(id: string | number) {
    this.products.update(current =>
      current.filter(p => String(p.id) !== String(id))
    );
  }

  private loadData() {
    if (this.loading() || this.isLastPage) return;

    this.loading.set(true);

    this.fetchDataFn(this.currentPage, this.pageSize)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response: any) => {
          if (response.content && response.content.length > 0) {
            this.products.update(current => [...current, ...response.content]);

            const currentPageNumber = response.page.number;
            const totalPages = response.page.totalPages;

            this.isLastPage = currentPageNumber >= (totalPages - 1);

            if (!this.isLastPage) {
              this.currentPage++;
            }
          } else {
            this.isLastPage = true;
          }
        },
        error: (err) => console.error('Data loading error:', err)
      });
  }

  private setupObserver() {
    this.observer?.disconnect();

    this.observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && !this.loading() && !this.isLastPage) {
        this.loadData();
      }
    }, {
      threshold: 0.1,
      rootMargin: '200px'
    });

    if (this.sentinel) {
      this.observer.observe(this.sentinel.nativeElement);
    }
  }
}
