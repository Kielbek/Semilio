import {
  Component,
  computed,
  ElementRef,
  EventEmitter,
  forwardRef,
  HostListener,
  inject,
  Input,
  Output,
  signal
} from '@angular/core';
import {CommonModule} from '@angular/common';

import {
  ArrowLeft,
  Baby,
  Briefcase,
  ChevronRight,
  Cloudy,
  Columns2,
  Dumbbell,
  Flower2,
  Footprints,
  Heart,
  Layers,
  LUCIDE_ICONS,
  LucideAngularModule,
  LucideIconProvider,
  Mars,
  Rocket,
  Shirt,
  ShoppingBag,
  Smile,
  Sparkles,
  ThermometerSnowflake,
  UtilityPole,
  Venus,
  Watch,
  ChevronDown
} from 'lucide-angular';
import {ICategory} from '../../../../core/models/i-category';
import {NG_VALUE_ACCESSOR} from '@angular/forms';


@Component({
  selector: 'app-category-select',
  standalone: true,
  imports: [
    CommonModule,
    LucideAngularModule
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CategorySelect),
      multi: true
    },
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({
        Venus, Shirt, Sparkles, Layers, Columns2, Cloudy, UtilityPole,
        ThermometerSnowflake, Briefcase, Heart, Dumbbell, Footprints,
        ShoppingBag, Mars, Watch, Baby, Flower2, Rocket, Smile,
        ArrowLeft, ChevronRight, ChevronDown
      })
    }
  ],
  templateUrl: './category-select.component.html',
  styleUrl: './category-select.component.css'
})
export class CategorySelect {
  @Input() categories: ICategory[] = [];

  isOpen = signal(false);
  selectedPath = signal<string>('');

  historyStack = signal<ICategory[]>([]);

  currentNodes = computed(() => {
    const stack = this.historyStack();
    if (stack.length === 0) return this.categories;
    return stack[stack.length - 1].subcategories || [];
  });

  currentTitle = computed(() => {
    const stack = this.historyStack();
    return stack.length > 0 ? stack[stack.length - 1].name : 'Wybierz kategorię';
  });

  value: number | null = null;
  onChange: (value: number | null) => void = () => {};
  onTouched: () => void = () => {};
  isDisabled = false;

  writeValue(obj: any): void {
    this.value = obj;
  }
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled = isDisabled; }

  toggleDropdown() {
    if (this.isDisabled) return;
    this.isOpen.update(v => !v);
    if (this.isOpen()) {
      this.historyStack.set([]); // Reset przy otwarciu
    } else {
      this.onTouched(); // Oznaczamy pole jako "dotknięte" przy zamknięciu
    }
  }

  selectNode(node: ICategory) {
    if (node.subcategories && node.subcategories.length > 0) {
      this.historyStack.update(stack => [...stack, node]);
    } else {
      this.value = node.id;
      this.onChange(this.value);

      const pathNames = [...this.historyStack().map(n => n.name), node.name];
      this.selectedPath.set(pathNames.join(' › '));

      this.isOpen.set(false);
    }
  }

  goBack() {
    this.historyStack.update(stack => stack.slice(0, -1));
  }

  private eRef = inject(ElementRef);
  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if(!this.eRef.nativeElement.contains(event.target)) {
      if (this.isOpen()) {
        this.isOpen.set(false);
        this.onTouched();
      }
    }
  }
}
