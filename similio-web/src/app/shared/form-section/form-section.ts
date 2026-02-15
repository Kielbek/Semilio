import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-form-section',
  imports: [],
  templateUrl: './form-section.html',
  styleUrl: './form-section.css'
})
export class FormSection {
  @Input({ required: true }) title!: string;
  @Input() subtitle?: string;
}
