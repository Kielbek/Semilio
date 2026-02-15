import {Component, Input} from '@angular/core';
import {RouterLink} from '@angular/router';
import {NgTemplateOutlet} from '@angular/common';
import {Spinner} from '../spinner/spinner';

@Component({
  selector: 'app-button',
  imports: [
    RouterLink,
    NgTemplateOutlet,
    Spinner,
  ],
  templateUrl: './button.html',
  styleUrl: './button.css'
})
export class Button {
  @Input() variant: 'primary' | 'secondary' = 'primary';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() disabled: boolean = false;
  @Input() href: string | null = null;
  @Input() fullWidth: boolean = false;
  @Input() extraClass: string = '';
  @Input() isLoading: boolean = false;

  get buttonClasses(): string {
    let classes = 'font-semibold px-6 py-2.5 flex items-center justify-center gap-2 transition-all duration-300 ease-out rounded-sm ';

    if (this.fullWidth) {
      classes += 'w-full ';
    }

    if (this.disabled) {
      classes += 'opacity-50 cursor-not-allowed pointer-events-none ';
    } else {
      classes += 'cursor-pointer ';
    }

    if (this.variant === 'secondary') {
      classes += 'text-gray-800 border border-[var(--deep-blue)] bg-white hover:bg-gray-100 ';
    } else {
      classes += 'text-white bg-[var(--deep-blue)] hover:bg-[#034047] ';
    }

    return classes + this.extraClass;
  }
}
