import {Component, Input} from '@angular/core';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-accordion-item',
  imports: [
    NgClass
  ],
  templateUrl: './accordion-item.html',
  styleUrl: './accordion-item.css',
})
export class AccordionItem {
  @Input() title: string = '';
  @Input() iconPath: string = '';

  isOpen = false;

  toggle() {
    this.isOpen = !this.isOpen;
  }
}
