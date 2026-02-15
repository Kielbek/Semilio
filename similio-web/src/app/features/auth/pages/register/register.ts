import {Component, EventEmitter, inject, OnInit, Output, signal} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup, NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import {AuthService} from '../../../../core/service/auth-service';
import {Button} from '../../../../shared/button/button';
import {CustomValidators} from '../../../../core/validators/custom-validators';
import {COUNTRIES} from '../../../../core/constants/countries';
import {finalize} from 'rxjs';
import {InputField} from '../../../../shared/input-field/input-field';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    Button,
    InputField
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);

  @Output() loginClick = new EventEmitter<void>();
  @Output() registerSuccess = new EventEmitter<void>();

  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  readonly countries = COUNTRIES;

  readonly form = this.fb.group(
    {
      firstName: [
        '',
        [
          Validators.required,
          Validators.minLength(2), // Zazwyczaj min 2 znaki dla imienia
          Validators.maxLength(50),
          Validators.pattern(CustomValidators.NAME_PATTERN),
        ],
      ],
      lastName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50),
          Validators.pattern(CustomValidators.NAME_PATTERN),
        ],
      ],
      email: ['', [Validators.required, Validators.email]],
      countryCode: ['+48'],
      phoneNumber: [
        '',
        [Validators.required, Validators.pattern(CustomValidators.PHONE_PATTERN)],
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(72),
          Validators.pattern(CustomValidators.PASSWORD_PATTERN),
        ],
      ],
      confirmPassword: ['', [Validators.required]],
    },
    {
      validators: CustomValidators.match('password', 'confirmPassword'),
    }
  );

  getControl(name: string) {
    return this.form.get(name) as any;
  }

  get passwordValue(): string {
    return this.form.controls.password.value;
  }

  get hasUpperCase(): boolean {
    return CustomValidators.hasUpperCase(this.passwordValue);
  }

  get hasNumber(): boolean {
    return CustomValidators.hasNumber(this.passwordValue);
  }

  get isLengthValid(): boolean {
    return CustomValidators.isLengthValid(this.passwordValue, 8);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // Pokaż błędy walidacji użytkownikowi
      this.errorMessage.set('Formularz zawiera błędy. Sprawdź poprawność danych.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    // Pobieramy surowe wartości (bezpieczniejsze niż .value przy NonNullable)
    const formValue = this.form.getRawValue();
    const fullPhoneNumber = `${formValue.countryCode}${formValue.phoneNumber}`;

    this.authService
      .register(
        formValue.firstName,
        formValue.lastName,
        formValue.email,
        fullPhoneNumber,
        formValue.password,
        formValue.confirmPassword
      )
      .pipe(
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
        next: () => {
          this.successMessage.set('Rejestracja zakończona sukcesem! Możesz się teraz zalogować.');
          this.form.reset();
          this.registerSuccess.emit();
        },
        error: (err) => {
          console.error(err);
          this.errorMessage.set(
            typeof err?.error === 'string'
              ? err.error
              : 'Wystąpił błąd podczas rejestracji. Spróbuj ponownie.'
          );
        },
      });
  }
}
