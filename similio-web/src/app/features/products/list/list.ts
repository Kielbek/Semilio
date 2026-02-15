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

  private currentPage = 0;
  private isLastPage = false;
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
    // Odpinamy słuchaczy i obserwatora
    window.removeEventListener('scroll', this.onWindowScroll);
    this.observer?.disconnect();

    // Zapisujemy stan tylko jeśli mamy poprawny klucz i dane
    if (this.cacheKey && !this.cacheKey.includes('null') && this.products().length > 0) {
      this.listStateService.saveState(this.cacheKey, {
        products: this.products(),
        currentPage: this.currentPage,
        isLastPage: this.isLastPage,
        scrollPosition: this.currentScrollY // Zapisujemy ostatnią znaną pozycję
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

    // Skaczemy natychmiastowo
    setTimeout(() => {
      window.scrollTo({
        top: state.scrollPosition,
        behavior: 'get' as any // Hack: niektóre przeglądarki lepiej reagują na brak definicji lub 'instant'
      });
      // Standardowy, najszybszy skok:
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
  }

  removeProductById(id: number) {
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
        next: (page) => {
          this.products.update(current => [...current, ...page.content]);
          this.isLastPage = page.last;
          this.currentPage++;
        },
        error: (err) => console.error('Data loading error:', err)
      });
  }

  private setupObserver() {
    this.observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && this.products().length > 0) {
        this.loadData();
      }
    }, { threshold: 0.1 });

    if (this.sentinel) {
      this.observer.observe(this.sentinel.nativeElement);
    }
  }
}
