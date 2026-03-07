import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {AccordionItem} from '../../../../shared/accordion-item/accordion-item';

@Component({
  selector: 'app-safety',
  imports: [
    RouterLink,
    AccordionItem
  ],
  templateUrl: './safety.html',
  styleUrl: './safety.css'
})
export class Safety {

}
