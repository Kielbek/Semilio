import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {AccordionItem} from '../../../../shared/accordion-item/accordion-item';

@Component({
  selector: 'app-selling',
  imports: [
    RouterLink,
    AccordionItem
  ],
  templateUrl: './selling.html',
  styleUrl: './selling.css'
})
export class Selling {

}
