import {Component, DestroyRef, inject, OnInit, signal} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Select, SelectOption} from '../../../../shared/select/select';
import {ArrowLeft, Filter, LUCIDE_ICONS, LucideAngularModule, LucideIconProvider} from 'lucide-angular';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {debounceTime, map, Observable} from 'rxjs';
import {InputField} from '../../../../shared/input-field/input-field';
import {Button} from '../../../../shared/button/button';
import {AsyncPipe, NgClass} from '@angular/common';
import {DictionaryService} from '../../../../core/service/dictionary-service';
import {CONDITION_OPTIONS} from '../../../../core/models/product/condition';

interface FilterForm {
  priceFrom: FormControl<number | null>;
  priceTo: FormControl<number | null>;
  condition: FormControl<string | null>;
  size: FormControl<string | null>;
  color: FormControl<string | null>;
  brand: FormControl<string | null>;
  sort: FormControl<string>;
}

@Component({
  selector: 'app-search-filters',
  imports: [ReactiveFormsModule, FormsModule, Select, LucideAngularModule, InputField, Button, NgClass, AsyncPipe],
  templateUrl: './search-filters.html',
  styleUrl: './search-filters.css',
  providers: [
    { provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ Filter, ArrowLeft }) }
  ]
})
export class SearchFilters implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dictionaryService = inject(DictionaryService);

  readonly isMobileOpen = signal(false);

  readonly filterForm = new FormGroup<FilterForm>({
    priceFrom: new FormControl(null),
    priceTo: new FormControl(null),
    condition: new FormControl(null),
    size: new FormControl(null),
    color: new FormControl(null),
    brand: new FormControl(null),
    sort: new FormControl('recommended', { nonNullable: true }),
  });

  readonly conditions: SelectOption[] = CONDITION_OPTIONS;

  readonly sortOptions = [
    { value: 'recommended', label: 'Polecane' },
    { value: 'newest', label: 'Najnowsze' },
    { value: 'price_asc', label: 'Najtańsze' },
    { value: 'price_desc', label: 'Najdroższe' }
  ];

  readonly brands$ = this.dictionaryService.dictionaries$.pipe(
    map(dict => dict.brands.map(b => ({ value: b.id.toString(), label: b.name })))
  );

  readonly sizes$: Observable<{value: string, label: string}[]> = this.dictionaryService.dictionaries$.pipe(
    map(dict => {
      const allSizesFlat = Object.values(dict.sizes || {}).flat();
      return allSizesFlat.map(s => ({
        value: s.id.toString(),
        label: s.name
      }));
    })
  );

  readonly colors$ = this.dictionaryService.dictionaries$.pipe(
    map(dict => dict.colors.map(c => ({
      value: c.id.toString(),
      label: c.name,
      color: c.hexCode
    })))
  );

  ngOnInit() {
    this.syncWithUrl();
    this.listenToChanges();
  }

  toggleMobile() {
    this.isMobileOpen.update(v => !v);
    document.body.style.overflow = this.isMobileOpen() ? 'hidden' : '';
  }

  apply() {
    const rawValue = this.filterForm.getRawValue();

    const currentParams = this.route.snapshot.queryParams;

    const filterParams: any = {};
    Object.entries(rawValue).forEach(([key, value]) => {
      if (value === null || value === '' || (key === 'sort' && value === 'recommended')) {
        filterParams[key] = null;
      } else {
        filterParams[key] = value;
      }
    });

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        ...currentParams,
        ...filterParams
      },
    });

    if (this.isMobileOpen()) this.toggleMobile();
  }

  clear() {
    this.filterForm.reset({ sort: 'recommended' });
    this.apply();
  }

  private syncWithUrl() {
    this.route.queryParams.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      this.filterForm.patchValue({
        priceFrom: params['priceFrom'] ? Number(params['priceFrom']) : null,
        priceTo: params['priceTo'] ? Number(params['priceTo']) : null,
        condition: params['condition'] || null,

        size: params['size'] || null,
        color: params['color'] || null,
        brand: params['brand'] || null,

        sort: params['sort'] || 'recommended'
      }, { emitEvent: false });
    });
  }

  private listenToChanges() {
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => {
      if (!this.isMobileOpen()) this.apply();
    });
  }

  getControl(name: string): FormControl {
    return this.filterForm.get(name) as FormControl;
  }
}
