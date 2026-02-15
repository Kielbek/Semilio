import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Toast} from '../toast.component';
import { IToast } from '../../../core/models/i-toast';

@Component({
  selector: 'app-toast-item',
  imports: [],
  templateUrl: './toast-item.html',
  styleUrl: './toast-item.css'
})
export class ToastItem {
  @Input({ required: true }) toast!: IToast;
  @Output() close = new EventEmitter<number>();

  onClose() {
    this.close.emit(this.toast.id);
  }
}
