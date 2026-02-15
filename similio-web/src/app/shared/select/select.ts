import { Component, ElementRef, forwardRef, HostListener, Input, OnDestroy } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ArrowLeft, Check, ChevronDown, LUCIDE_ICONS, LucideAngularModule, LucideIconProvider } from 'lucide-angular';

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
  imports: [LucideAngularModule],
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
      useValue: new LucideIconProvider({ ArrowLeft, Check, ChevronDown })
    }
  ],
})
export class Select implements ControlValueAccessor, OnDestroy {
  @Input() label = '';
  @Input() options: SelectOption[] = [];
  @Input() placeholder = 'Wybierz...';

  selectedOption: SelectOption | null = null;
  isOpen = false;

  private scrollPosition = 0;
  private readonly stateKey = `select_${Math.random().toString(36).substring(2, 9)}`;

  constructor(private eRef: ElementRef) {}

  @HostListener('window:popstate')
  onPopState(): void {
    if (this.isOpen) {
      this.handleClosing();
    }
  }

  @HostListener('document:click', ['$event.target'])
  onClickOutside(target: EventTarget | null): void {
    if (this.isOpen && target instanceof Node && !this.eRef.nativeElement.contains(target)) {
      this.closeManual();
    }
  }

  toggle(): void {
    this.isOpen ? this.closeManual() : this.open();
  }

  open(): void {
    if (this.isOpen) return;

    this.isOpen = true;
    this.lockScroll();
    window.history.pushState({ [this.stateKey]: true }, '');
  }

  closeManual(): void {
    if (!this.isOpen) return;

    if (window.history.state?.[this.stateKey]) {
      window.history.back();
    } else {
      this.handleClosing();
    }
  }

  select(option: SelectOption): void {
    this.selectedOption = option;
    this.onChange(option.value);
    this.onTouched();
    this.closeManual();
  }

  private handleClosing(): void {
    this.isOpen = false;
    this.unlockScroll();
  }

  private lockScroll() {
    this.scrollPosition = window.scrollY;
    document.body.style.overflow = 'hidden';
    if (window.innerWidth < 768) {
      document.body.style.position = 'fixed';
      document.body.style.top = `-${this.scrollPosition}px`;
      document.body.style.width = '100%';
    }
  }

  private unlockScroll() {
    document.body.style.overflow = '';
    document.body.style.position = '';
    document.body.style.top = '';
    document.body.style.width = '';
    window.scrollTo(0, this.scrollPosition);
  }

  ngOnDestroy(): void {
    this.unlockScroll();
  }

  // --- ControlValueAccessor ---

  onChange: (value: any) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: any): void {
    this.selectedOption = this.options?.find(o => o.value === value) || null;
  }

  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
}
