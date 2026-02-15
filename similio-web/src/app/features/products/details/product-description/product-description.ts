import {Component, Input} from '@angular/core';
import {ConditionLabelPipe} from '../../../../core/pipes/condition-label-pipe';
import {IProduct} from '../../../../core/models/product/i-product';

@Component({
  selector: 'app-product-description',
  imports: [
    ConditionLabelPipe,
  ],
  templateUrl: './product-description.html',
  styleUrl: './product-description.css',
})
export class ProductDescription {
  @Input({ required: true }) product?: IProduct;
}
