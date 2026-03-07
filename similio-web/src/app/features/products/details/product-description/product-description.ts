import {Component, EventEmitter, Input, Output} from '@angular/core';
import {IProduct} from '../../../../core/models/product/i-product';
import {ConditionLabelPipe} from '../../../../core/pipes/condition-label-pipe';

@Component({
  selector: 'app-product-description',
  imports: [
    ConditionLabelPipe
  ],
  templateUrl: './product-description.html',
  styleUrl: './product-description.css',
})
export class ProductDescription {
  @Input({ required: true }) product?: IProduct;
  @Output() emitRepostEvent = new EventEmitter<void>();
}
