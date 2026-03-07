import {
  Component,
  computed,
  ElementRef,
  forwardRef,
  HostListener,
  inject,
  Input,
  OnChanges,
  SimpleChanges,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';
import {
  ArrowLeft,
  Briefcase,
  ChevronDown,
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
  Baby,
  Check
} from 'lucide-angular';
import { ICategory } from '../../../../core/models/i-category';

@Component({
  selector: 'app-category-select',
  standalone: true,
  imports: [
    CommonModule,
    LucideAngularModule
  ],
  templateUrl: './category-select.component.html',
  styleUrl: './category-select.component.css',
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
        ArrowLeft, ChevronRight, ChevronDown, Check
      })
    }
  ],
})
export class CategorySelect implements ControlValueAccessor, OnChanges {
  @Input({ required: true }) control!: FormControl;
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
  isDisabled = false;

  onChange: (value: number | null) => void = () => {};
  onTouched: () => void = () => {};

  private eRef = inject(ElementRef);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['categories'] && this.value) {
      this.reconstructStateFromValue(this.value);
    }
  }

  writeValue(obj: any): void {
    this.value = obj;
    if (obj && this.categories.length > 0) {
      this.reconstructStateFromValue(obj);
    }
  }

  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled = isDisabled; }

  toggleDropdown() {
    if (this.isDisabled) return;
    this.isOpen.update(v => !v);

    if (!this.isOpen()) {
      this.onTouched();
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

  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if(!this.eRef.nativeElement.contains(event.target)) {
      if (this.isOpen()) {
        this.isOpen.set(false);
        this.onTouched();
      }
    }
  }

  private reconstructStateFromValue(targetId: number) {
    const path: ICategory[] = [];
    const found = this.findPath(this.categories, targetId, path);

    if (found) {
      this.selectedPath.set(path.map(c => c.name).join(' › '));

      if (path.length > 1) {
        this.historyStack.set(path.slice(0, -1));
      } else {
        this.historyStack.set([]);
      }
    }
  }

  private findPath(nodes: ICategory[], targetId: number, currentPath: ICategory[]): boolean {
    for (const node of nodes) {
      currentPath.push(node);

      if (node.id === targetId) {
        return true;
      }

      if (node.subcategories && node.subcategories.length > 0) {
        if (this.findPath(node.subcategories, targetId, currentPath)) {
          return true;
        }
      }

      currentPath.pop();
    }
    return false;
  }
}
