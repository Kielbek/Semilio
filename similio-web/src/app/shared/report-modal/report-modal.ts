import {Component, computed, EventEmitter, inject, Input, Output, signal} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {
  AlertTriangle,
  ArrowLeft,
  Ban,
  Check,
  ChevronRight,
  Copyright,
  FileWarning,
  Gavel,
  LUCIDE_ICONS,
  LucideAngularModule,
  LucideIconProvider,
  ShieldAlert,
  X
} from 'lucide-angular';
import {Button} from '../button/button';
import {UiModal} from '../ui-modal/ui-modal';
import {ReportService} from '../../core/service/report-service';
import {ToastService} from '../../core/service/toast-service';
import {toSignal} from '@angular/core/rxjs-interop';
import {TextareaField} from '../textarea-field/textarea-field';

export interface SubcategoryResponse {
  id: string;
  label: string;
  description: string;
}

export interface CategoryResponse {
  id: string;
  label: string;
  description: string;
  subcategories: SubcategoryResponse[];
}

export enum ReportReason {
  ILLEGAL_CONTENT = 'ILLEGAL_CONTENT',
  COPYRIGHT = 'COPYRIGHT',
  SCAM = 'SCAM',
  HATE_SPEECH = 'HATE_SPEECH',
  PROHIBITED_ITEM = 'PROHIBITED_ITEM',
  OTHER = 'OTHER'
}

@Component({
  selector: 'app-report-modal',
  imports: [
    LucideAngularModule,
    ReactiveFormsModule,
    Button,
    UiModal,
    TextareaField
  ],
  templateUrl: './report-modal.html',
  styleUrl: './report-modal.css',
  providers: [
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({
        ArrowLeft,
        X,
        ChevronRight,
        Check,
        AlertTriangle,
        ShieldAlert,
        FileWarning,
        Ban,
        Copyright,
        Gavel
      })
    }
  ]
})
export class ReportModal {
  private fb = inject(FormBuilder);
  private reportService = inject(ReportService);
  private toastService = inject(ToastService);

  @Input({ required: true }) targetId!: string;
  @Output() close = new EventEmitter<void>();

  step = signal<1 | 2>(1);
  selectedCategory = signal<CategoryResponse | null>(null);
  isSubmitting = signal<boolean>(false);

  categories = this.reportService.categories;

  isLoadingCategories = computed(() => !this.categories() || this.categories().length === 0);

  form = this.fb.group({
    subcategoryId: [null as string | null],
    description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
    declaration: [true, Validators.requiredTrue]
  });

  descriptionValue = toSignal(this.form.controls.description.valueChanges, { initialValue: '' });
  descriptionLength = computed(() => this.descriptionValue()?.length || 0);

  selectCategory(category: CategoryResponse) {
    this.selectedCategory.set(category);
    this.step.set(2);
    this.form.reset({ declaration: true });

    const subcatControl = this.form.controls.subcategoryId;
    if (category.subcategories && category.subcategories.length > 0) {
      subcatControl.setValidators([Validators.required]);
    } else {
      subcatControl.clearValidators();
    }
    subcatControl.updateValueAndValidity();
  }

  selectSubcategory(subId: string) {
    if (this.form.controls.subcategoryId.value === subId) {
      this.form.patchValue({ subcategoryId: null });
    } else {
      this.form.patchValue({ subcategoryId: subId });
    }
    this.form.markAsDirty();
  }

  goBack() {
    this.step.set(1);
    this.selectedCategory.set(null);
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const category = this.selectedCategory();
    if (!category) return;

    this.isSubmitting.set(true);

    const request: any = {
      targetId: this.targetId,
      reason: category.id,
      subReasonId: this.form.value.subcategoryId || null,
      description: this.form.value.description!,
      declaration: this.form.value.declaration!
    };

    this.reportService.submitReport(request).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.close.emit();
        this.toastService.show('Dziękujemy. Zgłoszenie zostało wysłane do weryfikacji.', 'success');
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const message = err.error?.message || 'Something went wrong. Please try again.';
        this.close.emit();
        this.toastService.show(message, 'error');
      }
    });
  }
}
