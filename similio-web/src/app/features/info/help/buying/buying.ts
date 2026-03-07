import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {AccordionItem} from '../../../../shared/accordion-item/accordion-item';

@Component({
  selector: 'app-buying',
  imports: [
    RouterLink,
    AccordionItem
  ],
  templateUrl: './buying.html',
  styleUrl: './buying.css'
})
export class Buying {

}
