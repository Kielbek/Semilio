import {Component, Input} from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {NgClass} from '@angular/common';

@Component({
  selector: 'app-input-field',
  imports: [
    ReactiveFormsModule,
    NgClass
  ],
  templateUrl: './input-field.html',
  styleUrl: './input-field.css'
})
export class InputField {
  @Input({ required: true }) control!: FormControl;
  @Input({ required: true }) label!: string;
  @Input() type: 'text' | 'number' | 'email' | 'password' = 'text';
  @Input() placeholder = '';
  @Input() suffix?: string;

  isPasswordVisible = false;

  get inputType(): string {
    if (this.type === 'password') {
      return this.isPasswordVisible ? 'text' : 'password';
    }
    return this.type;
  }

  togglePassword(): void {
    this.isPasswordVisible = !this.isPasswordVisible;
  }
}
