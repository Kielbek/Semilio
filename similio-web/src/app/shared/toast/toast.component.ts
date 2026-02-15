import {Component} from '@angular/core';
import {AsyncPipe} from '@angular/common';
import {ToastItem} from './toast-item/toast-item';
import {ToastService} from '../../core/service/toast-service';

@Component({
  selector: 'app-toast',
  imports: [
    AsyncPipe,
    ToastItem
  ],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css',
})
export class Toast {
  constructor(public toastService: ToastService) {}

}
