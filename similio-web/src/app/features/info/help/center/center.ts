import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {AccordionItem} from '../../../../shared/accordion-item/accordion-item';

@Component({
  selector: 'app-center',
  imports: [
    RouterLink,
    AccordionItem
  ],
  templateUrl: './center.html',
  styleUrl: './center.css'
})
export class Center {

}
