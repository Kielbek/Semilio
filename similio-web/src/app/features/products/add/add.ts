import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { BehaviorSubject, combineLatest, finalize, map, Observable, startWith } from 'rxjs';

import { FormSection } from '../../../shared/form-section/form-section';
import { InputField } from '../../../shared/input-field/input-field';
import { Button } from '../../../shared/button/button';
import { Select } from '../../../shared/select/select';
import { TextareaField } from '../../../shared/textarea-field/textarea-field';
import { ImageUploader } from '../components/image-uploader/image-uploader';
import { CategorySelect } from '../components/category-select/category-select.component';

import { CategoryService } from '../../../core/service/category-service';
import { ProductService } from '../../../core/service/product.service';
import { DictionaryService } from '../../../core/service/dictionary-service';
import { ICategory } from '../../../core/models/i-category';
import { CONDITION_OPTIONS } from '../../../core/models/product/condition';
import { IImage } from '../../../core/models/product/i-image';
import { noWhitespaceValidator } from '../../../core/validators/no-whitespace-validator';
import {ListStateService} from '../../../core/service/list-state-service';

interface SelectOption {
  value: number;
  label: string;
}

@Component({
  selector: 'app-add',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormSection,
    InputField,
    ImageUploader,
    CategorySelect,
    Button,
    Select,
    TextareaField,
    AsyncPipe
  ],
  templateUrl: './add.html',
  styleUrl: './add.css',
})
export class Add implements OnInit {
  private readonly dictionaryService = inject(DictionaryService);
  private readonly fb = inject(FormBuilder);
  private readonly categoryService = inject(CategoryService);
  private readonly productService = inject(ProductService);
  private readonly listStateService = inject(ListStateService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly conditionOptions = CONDITION_OPTIONS;
  readonly maxImage = 9;

  readonly loading = signal(false);
  readonly isEditMode = signal(false);

  productId: string | null = null;
  files: File[] = [];
  existingImages: IImage[] = [];
  imagesToKeep: string[] = [];
  categories: ICategory[] = [];

  private readonly categoriesSubject = new BehaviorSubject<ICategory[]>([]);

  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100), noWhitespaceValidator()]],
    description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(2000), noWhitespaceValidator()]],
    amount: [0, [Validators.required, Validators.min(1), Validators.max(9999999), Validators.pattern(/^\d+(\.\d{1,2})?$/)]],
    categoryId: [0, [Validators.required, Validators.min(1)]],
    brandId: [null as number | null, [Validators.required]],
    colorId: [null as number | null, [Validators.required]],
    sizeId: [null as number | null, [Validators.required]],
    condition: ['', [Validators.required]],
    imageCount: [0, [Validators.required, Validators.min(1), Validators.max(this.maxImage)]]
  });

  readonly brands$ = this.dictionaryService.dictionaries$.pipe(
    map(dict => dict.brands.map(b => ({ value: b.id, label: b.name })))
  );

  readonly colors$ = this.dictionaryService.dictionaries$.pipe(
    map(dict => dict.colors.map(c => ({
      value: c.id,
      label: c.name,
      color: c.hexCode
    })))
  );

  readonly sizes$: Observable<SelectOption[]> = combineLatest([
    this.dictionaryService.dictionaries$.pipe(map(dict => dict.sizes)),
    this.form.get('categoryId')!.valueChanges.pipe(
      startWith(this.form.get('categoryId')?.value)
    ),
    this.categoriesSubject.asObservable()
  ]).pipe(
    map(([sizesMap, selectedCategoryId, categories]) => {
      const catId = Number(selectedCategoryId);
      if (!catId || categories.length === 0) {
        return this.mapToSelectOptions(sizesMap['OTHER'] || []);
      }

      const result = this.findCategoryWithInheritance(categories, catId);
      const effectiveType = result?.effectiveType && result.effectiveType !== 'NONE'
        ? result.effectiveType
        : 'OTHER';

      const matchedSizes = sizesMap[effectiveType] || sizesMap['OTHER'] || [];
      return this.mapToSelectOptions(matchedSizes);
    })
  );

  ngOnInit(): void {
    this.loadCategories();
    this.checkEditMode();
    this.setupCategoryChangeListener();
  }

  getControl(name: string): FormControl {
    return this.form.get(name) as FormControl;
  }

  onFilesChanged(newFiles: File[]): void {
    this.files = newFiles;
    this.updateImageCount();
  }

  onExistingImagesChanged(keptUrls: string[]): void {
    this.imagesToKeep = keptUrls;
    this.updateImageCount();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.scrollToFirstError();
      return;
    }

    this.loading.set(true);
    const productData = this.form.getRawValue();

    if (this.isEditMode() && this.productId) {
      this.handleUpdate(this.productId, productData);
    } else {
      this.handleCreate(productData);
    }
  }

  private loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.categoriesSubject.next(data);
      },
      error: (err) => console.error('Failed to load categories', err)
    });
  }

  private checkEditMode(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.productId = idParam;
      this.isEditMode.set(true);
      this.loadProductData(this.productId);
    }
  }

  private setupCategoryChangeListener(): void {
    this.form.get('categoryId')?.valueChanges.subscribe(() => {
      this.form.get('sizeId')?.setValue(null);
    });
  }

  private mapToSelectOptions(sizes: any[]): SelectOption[] {
    return sizes.map(s => ({ value: s.id, label: s.name }));
  }

  private loadProductData(id: string): void {
    this.loading.set(true);
    this.productService.getProductById(id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (product) => {
          this.form.patchValue({
            title: product.title,
            description: product.description,
            amount: product.price.amount,
            categoryId: product.categoryId,
            brandId: product.brand?.id,
            colorId: product.color?.id,
            sizeId: product.size?.id,
            condition: product.condition,
            imageCount: product.images.length,
          });

          if (product.images) {
            this.existingImages = product.images;
            this.imagesToKeep = product.images.map(img => img.url);
          }
        },
        error: () => this.router.navigate(['/'])
      });
  }

  private handleCreate(data: any): void {
    this.productService.createProduct(data, this.files)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.listStateService.clearByPrefix('profile-ads-me');

          this.router.navigate(['/create-success'], { state: { product: response } });
        },
        error: (err) => console.error('Create error', err)
      });
  }

  private handleUpdate(id: string, data: any): void {
    const updateData = { ...data, remainingImages: this.imagesToKeep };
    this.productService.updateProduct(id, updateData, this.files)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response: any) => this.router.navigate(['/product-details', response.slug]),
        error: (err) => console.error('Update error', err)
      });
  }

  private scrollToFirstError(): void {
    setTimeout(() => {
      const firstInvalidControl = document.querySelector(
        'input.ng-invalid, textarea.ng-invalid, select.ng-invalid, ' +
        'app-select.ng-invalid, app-category-select.ng-invalid, ' +
        'app-image-uploader.ng-invalid'
      );
      if (firstInvalidControl) {
        firstInvalidControl.scrollIntoView({ behavior: 'smooth', block: 'center' });
        (firstInvalidControl as HTMLElement).focus();
      }
    }, 100);
  }

  private updateImageCount(): void {
    const retainedImagesCount = this.existingImages.filter(img => this.imagesToKeep.includes(img.url)).length;
    const total = this.files.length + retainedImagesCount;

    this.form.patchValue({ imageCount: total });
    this.form.get('imageCount')?.markAsTouched();
  }

  private findCategoryWithInheritance(
    categories: ICategory[],
    targetId: number,
    inheritedType?: string
  ): { category: ICategory, effectiveType?: string } | undefined {

    for (const cat of categories) {
      const currentEffectiveType = cat.sizeType || inheritedType;

      if (cat.id === targetId) {
        return { category: cat, effectiveType: currentEffectiveType };
      }

      if (cat.subcategories && cat.subcategories.length > 0) {
        const found = this.findCategoryWithInheritance(cat.subcategories, targetId, currentEffectiveType);
        if (found) return found;
      }
    }

    return undefined;
  }
}
