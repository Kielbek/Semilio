import { Component, computed, ElementRef, forwardRef, HostListener, Input, OnDestroy, signal } from '@angular/core';
import { ControlValueAccessor, FormControl, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ArrowLeft, Check, ChevronDown, Search, SearchX,
  LUCIDE_ICONS, LucideAngularModule, LucideIconProvider
} from 'lucide-angular';
import { InputField } from '../input-field/input-field';

export interface SelectOption {
  value: any;
  label: string;
  icon?: string;
  description?: string;
  color?: string;
}

@Component({
  selector: 'app-select',
  standalone: true,
  imports: [
    LucideAngularModule,
    ReactiveFormsModule,
    NgClass,
    FormsModule,
    InputField
  ],
  templateUrl: './select.html',
  styleUrl: './select.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => Select),
      multi: true
    },
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({ ArrowLeft, Check, ChevronDown, Search, SearchX })
    }
  ],
})
export class Select implements ControlValueAccessor, OnDestroy {
  @Input() label = '';
  @Input() placeholder = 'Wybierz...';
  @Input() control!: FormControl;

  @Input()
  set options(val: SelectOption[] | null) {
    this._optionsSignal.set(val ?? []);
    this.syncSelectedValue();
  }

  get options(): SelectOption[] {
    return this._optionsSignal();
  }

  readonly isOpen = signal(false);
  readonly searchQuery = signal('');
  readonly searchControl = new FormControl('', { nonNullable: true });

  selectedOption: SelectOption | null = null;

  private readonly _optionsSignal = signal<SelectOption[]>([]);
  private readonly _stateKey = `select_${Math.random().toString(36).substring(2, 9)}`;

  readonly filteredOptions = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const currentOptions = this._optionsSignal();

    if (!query) return currentOptions;

    return currentOptions.filter(opt =>
      opt.label.toLowerCase().includes(query)
    );
  });

  constructor(private readonly eRef: ElementRef) {
    this.setupSearchSubscription();
  }

  ngOnDestroy(): void {
    this.unlockScroll();
  }

  @HostListener('window:popstate')
  onPopState(): void {
    if (this.isOpen()) {
      this.close();
    }
  }

  @HostListener('document:click', ['$event.target'])
  onClickOutside(target: EventTarget | null): void {
    const clickedOutside = target instanceof Node && !this.eRef.nativeElement.contains(target);
    if (this.isOpen() && clickedOutside) {
      this.close();
    }
  }

  toggle(): void {
    this.isOpen() ? this.close() : this.open();
  }

  open(): void {
    if (this.isOpen()) return;

    this.isOpen.set(true);
    this.lockScroll();
    window.history.pushState({ [this._stateKey]: true }, '');
  }

  openDesktop(): void {
    if (!this.isOpen()) {
      this.isOpen.set(true);
      this.clearSearch();
    }
  }

  close(): void {
    if (!this.isOpen()) return;

    this.isOpen.set(false);
    this.unlockScroll();

    if (window.history.state?.[this._stateKey]) {
      window.history.back();
    }
  }

  selectOption(option: SelectOption): void {
    const isDeselection = this.selectedOption?.value === option.value;

    if (isDeselection) {
      this.selectedOption = null;
      this.clearSearch();
      this.onChange(null);
    } else {
      this.selectedOption = option;
      this.onChange(option.value);
    }

    this.onTouched();
    this.close();
  }

  onSearchChange(value: string): void {
    this.searchQuery.set(value);
  }

  clearSearch(event?: Event): void {
    event?.stopPropagation();
    this.searchQuery.set('');
    this.searchControl.setValue('', { emitEvent: false });
  }

  private setupSearchSubscription(): void {
    this.searchControl.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(value => this.searchQuery.set(value));
  }

  private syncSelectedValue(): void {
    if (this.control?.value != null) {
      this.writeValue(this.control.value);
    }
  }

  private lockScroll(): void {
    if (window.innerWidth < 768) {
      document.body.style.setProperty('overflow', 'hidden');
    }
  }

  private unlockScroll(): void {
    if (window.innerWidth < 768) {
      document.body.style.removeProperty('overflow');
    }
  }

  // --- Control Value Accessor ---
  onChange: (value: any) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: any): void {
    this.selectedOption = this._optionsSignal().find(o => o.value === value) || null;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }
}
