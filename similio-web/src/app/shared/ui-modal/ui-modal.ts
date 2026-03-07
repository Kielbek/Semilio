import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Spinner} from '../spinner/spinner';

@Component({
  selector: 'app-ui-modal',
  imports: [
    Spinner
  ],
  templateUrl: './ui-modal.html',
  styleUrl: './ui-modal.css',
})
export class UiModal {
  @Input() title: string = '';
  @Input() showBackButton: boolean = false;
  @Input() isLoading = false;

  @Output() close = new EventEmitter<void>();
  @Output() back = new EventEmitter<void>();

}
