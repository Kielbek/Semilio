import { Pipe, PipeTransform } from '@angular/core';
import {Color, ColorDisplay} from '../models/product/color';

@Pipe({
  name: 'colorLabel'
})
export class ColorLabelPipe implements PipeTransform {

  transform(value: Color | string | null | undefined): string {
    if (!value) return '';

    const colorKey = value as Color;

    return ColorDisplay[colorKey] || value.toString();
  }

}
