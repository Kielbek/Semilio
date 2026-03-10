import {
  Component,
  computed,
  ElementRef,
  forwardRef,
  HostListener,
  inject,
  Input,
  OnDestroy,
  signal
} from '@angular/core';
import {
  ControlValueAccessor,
  FormControl,
  FormsModule,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule
} from '@angular/forms';
import { DOCUMENT, NgClass } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
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
  private readonly document = inject(DOCUMENT);
  private readonly eRef = inject(ElementRef);

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
  readonly searchControl = new FormControl('', { nonNullable: true });
  readonly searchQuery = toSignal(this.searchControl.valueChanges, { initialValue: '' });

  selectedOption: SelectOption | null = null;

  private readonly _optionsSignal = signal<SelectOption[]>([]);
  private readonly _stateKey = `select_${Math.random().toString(36).substring(2, 9)}`;
  private _pendingValue: any = undefined;
  private scrollPosition = 0;

  readonly filteredOptions = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const currentOptions = this._optionsSignal();

    if (!query) return currentOptions;

    return currentOptions.filter(opt =>
      opt.label.toLowerCase().includes(query)
    );
  });

  ngOnDestroy(): void {
    if (this.isOpen()) {
      this.unlockScroll();
    }
  }

  @HostListener('window:popstate')
  onPopState(): void {
    if (this.isOpen()) {
      this.closeModalUI();
    }
  }

  @HostListener('document:click', ['$event.target'])
  onClickOutside(target: EventTarget | null): void {
    const clickedOutside = target instanceof Node && !this.eRef.nativeElement.contains(target);
    if (this.isOpen() && clickedOutside) {
      this.close();
    }
  }

  openDesktop(): void {
    this.open();
  }

  open(): void {
    if (this.isOpen()) return;

    this.isOpen.set(true);
    this.clearSearch();

    if (this.isMobile()) {
      window.history.pushState({ [this._stateKey]: true }, '');
    }

    this.lockScroll();
  }

  close(): void {
    if (!this.isOpen()) return;

    this.closeModalUI();

    if (typeof window !== 'undefined' && window.history.state?.[this._stateKey]) {
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
    this.searchControl.setValue(value);
  }

  clearSearch(event?: Event): void {
    event?.stopPropagation();
    this.searchControl.setValue('');
  }

  onChange: (value: any) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: any): void {
    this._pendingValue = value;
    this.syncSelectedValue();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  private syncSelectedValue(): void {
    if (this._pendingValue === undefined) return;

    const opts = this._optionsSignal();
    if (opts.length > 0) {
      this.selectedOption = opts.find(o => o.value === this._pendingValue) || null;
    }
  }

  private closeModalUI(): void {
    if (this.document.activeElement instanceof HTMLElement) {
      this.document.activeElement.blur();
    }

    this.isOpen.set(false);

    if (this.isMobile()) {
      setTimeout(() => {
        this.unlockScroll();
      }, 300);
    } else {
      this.unlockScroll();
    }
  }

  private lockScroll(): void {
    if (!this.isMobile()) return;

    this.scrollPosition = Math.round(window.scrollY);

    if ('scrollRestoration' in window.history) {
      window.history.scrollRestoration = 'manual';
    }

    this.document.body.style.position = 'fixed';
    this.document.body.style.top = `-${this.scrollPosition}px`;
    this.document.body.style.width = '100%';
    this.document.body.style.overflow = 'hidden';
  }

  private unlockScroll(): void {
    if (!this.isMobile()) return;

    const html = this.document.documentElement;
    const body = this.document.body;
    const pos = this.scrollPosition;

    html.style.setProperty('scroll-behavior', 'auto', 'important');

    body.style.removeProperty('position');
    body.style.removeProperty('top');
    body.style.removeProperty('width');
    body.style.removeProperty('overflow');

    window.scrollTo(0, pos);

    requestAnimationFrame(() => {
      html.style.removeProperty('scroll-behavior');
      if ('scrollRestoration' in window.history) {
        window.history.scrollRestoration = 'auto';
      }
    });
  }

  private isMobile(): boolean {
    return typeof window !== 'undefined' && window.innerWidth < 768;
  }
}
