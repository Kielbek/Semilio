import {Component, Input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-textarea-field',
  imports: [
    ReactiveFormsModule,
    NgClass
  ],
  templateUrl: './textarea-field.html',
  styleUrl: './textarea-field.css'
})
export class TextareaField {
  @Input({ required: true }) control!: FormControl;
  @Input({ required: true }) label!: string;
  @Input() placeholder = '';
  @Input() maxLength = 2000;
  @Input() rows = 5;
}
