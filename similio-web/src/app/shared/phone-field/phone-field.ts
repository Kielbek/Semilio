import { Component, Input } from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {NgClass} from '@angular/common';
import {COUNTRIES} from '../../core/constants/countries';

@Component({
  selector: 'app-phone-field',
  imports: [
    ReactiveFormsModule,
    NgClass
  ],
  templateUrl: './phone-field.html',
  styleUrl: './phone-field.css',
})
export class PhoneField {
  @Input({ required: true }) phoneControl!: FormControl;
  @Input({ required: true }) codeControl!: FormControl;
  @Input() label: string = 'Telefon';

  readonly countries = COUNTRIES;
}
