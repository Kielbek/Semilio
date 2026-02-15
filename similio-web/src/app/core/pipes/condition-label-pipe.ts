import { Pipe, PipeTransform } from '@angular/core';
import {Condition, ConditionDisplay} from '../models/product/condition';

@Pipe({
  name: 'conditionLabel'
})
export class ConditionLabelPipe implements PipeTransform {

  transform(value: Condition | string | null | undefined): string {
    if (!value) return '';

    const conditionKey = value as Condition;

    return ConditionDisplay[conditionKey] || value.toString();
  }
}
