import {Component, computed, EventEmitter, inject, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {Button} from '../../../../shared/button/button';
import {InputField} from '../../../../shared/input-field/input-field';
import {ChatService} from '../../../../core/service/chat-service';
import {UiModal} from '../../../../shared/ui-modal/ui-modal';

@Component({
  selector: 'app-propose-price',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Button,
    InputField,
    UiModal
  ],
  templateUrl: './propose-price.html',
  styleUrl: './propose-price.css'
})
export class ProposePrice {
  private readonly chatService = inject(ChatService);

  @Input() chatId?: string;
  @Input() productId?: string;
  @Input() productImage: string = '';
  @Input() productTitle: string = '';
  @Input() currentPrice: number = 0;
  @Output() close = new EventEmitter<void>();
  @Output() submitPrice = new EventEmitter<number>();

  priceControl = new FormControl<number | null>(null, [
    Validators.required,
    Validators.min(1)
  ]);

  protected readonly Math = Math;

  suggestedPrice = computed(() => Math.round(this.currentPrice * 0.9));

  get isPriceValid(): boolean {
    const val = this.priceControl.value;
    return this.priceControl.valid && val !== null && val < this.currentPrice;
  }

  setPrice(value: number) {
    this.priceControl.setValue(value);
    this.priceControl.markAsTouched();
  }

  onSend() {
    if (this.isPriceValid && this.priceControl.value) {
      this.chatService.sendProposal(this.chatId, this.productId, this.priceControl.value).subscribe({
        next: (response) => {
          this.close.emit();
        },
        error: (err) => console.error('Nie udało się wysłać oferty', err)
      });
    }
  }
}
