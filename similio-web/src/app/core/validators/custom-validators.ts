import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export class CustomValidators {
  static readonly NAME_PATTERN = /^[\p{L} '-]+$/u;

  static readonly PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{8,}$/;

  static readonly PHONE_PATTERN = /^\+?[1-9]\d{1,14}$/;

  static match(controlName: string, matchingControlName: string): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const control = group.get(controlName);
      const matchingControl = group.get(matchingControlName);

      if (!control || !matchingControl) {
        return null;
      }

      if (!control.value || !matchingControl.value) {
        return null;
      }

      if (control.value !== matchingControl.value) {
        return { mismatch: true };

        // Opcjonalnie: Możemy też ustawić błąd bezpośrednio na polu confirmPassword,
        // co ułatwia stylowanie inputa na czerwono:
        // matchingControl.setErrors({ ...matchingControl.errors, mismatch: true });
      }

      return null;
    };
  }

  static hasUpperCase(value: string | null): boolean {
    return /[A-Z]/.test(value || '');
  }

  static hasLowerCase(value: string | null): boolean {
    return /[a-z]/.test(value || '');
  }

  static hasNumber(value: string | null): boolean {
    return /[0-9]/.test(value || '');
  }

  static isLengthValid(value: string | null, minLength: number = 8): boolean {
    return (value || '').length >= minLength;
  }
}
