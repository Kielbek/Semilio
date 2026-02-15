import {Component, forwardRef, Input} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
  selector: 'app-toggle-switch',
  imports: [],
  templateUrl: './toggle-switch.html',
  styleUrl: './toggle-switch.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ToggleSwitch),
      multi: true
    }
  ],
})
export class ToggleSwitch implements ControlValueAccessor {
  @Input() id = `toggle-${Math.random().toString(36).substr(2, 9)}`;

  value = false;
  disabled = false;

  // Funkcje placeholderowe, które Angular nadpisze
  onChangeFn = (value: boolean) => {};
  onTouchedFn = () => {};

  onChange(event: any) {
    const target = event.target as HTMLInputElement;
    const isChecked = target.checked;

    this.value = isChecked;
    this.onChangeFn(isChecked);
  }

  onTouched() {
    this.onTouchedFn();
  }

  writeValue(value: boolean): void {
    this.value = value;
  }

  registerOnChange(fn: any): void {
    this.onChangeFn = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouchedFn = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }
}
