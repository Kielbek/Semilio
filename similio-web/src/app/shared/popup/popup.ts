import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';

@Component({
  selector: 'app-popup',
  imports: [],
  templateUrl: './popup.html',
  styleUrl: './popup.css',
})
export class Popup implements OnChanges {
  @Input() isOpen: boolean = false;
  @Output() close = new EventEmitter<void>();

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isOpen']) {
      if (this.isOpen) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = '';
      }
    }
  }

  onClose() {
    this.close.emit();
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboard(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      this.close.emit();
    }
  }
}
