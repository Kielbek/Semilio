import {Component, inject, OnInit, signal} from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../../../core/service/auth-service';
import {ToastService} from '../../../../core/service/toast-service';
import {Button} from '../../../../shared/button/button';
import {finalize} from 'rxjs';
import {CustomValidators} from '../../../../core/validators/custom-validators';
import {InputField} from '../../../../shared/input-field/input-field';

@Component({
  selector: 'app-reset-password',
  imports: [
    ReactiveFormsModule,
    Button,
    InputField
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPassword implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  private token: string | null = null;

  readonly form = this.fb.group({
    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(72),
        Validators.pattern(CustomValidators.PASSWORD_PATTERN) // Reużywalny regex
      ]
    ],
    confirmPassword: ['', [Validators.required]]
  }, {
    validators: CustomValidators.match('password', 'confirmPassword') // Reużywalny walidator
  });

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'];

    if (!this.token) {
      this.toastService.show('Nieprawidłowy link resetujący.', 'error');
      this.router.navigate(['/home']);
    }
  }

  getControl(name: string): FormControl {
    return this.form.get(name) as FormControl;
  }

  get passwordValue(): string {
    return this.form.controls.password.value;
  }

  // Delegacja do CustomValidators (DRY)
  get hasUpperCase(): boolean {
    return CustomValidators.hasUpperCase(this.passwordValue);
  }

  get hasNumber(): boolean {
    return CustomValidators.hasNumber(this.passwordValue);
  }

  get isLengthValid(): boolean {
    return CustomValidators.isLengthValid(this.passwordValue, 8);
  }

  onSubmit(): void {
    if (this.form.invalid || !this.token) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { password, confirmPassword } = this.form.getRawValue();

    this.authService.resetPassword(this.token, password, confirmPassword)
      .pipe(
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
        next: () => {
          this.toastService.show('Hasło zostało zmienione. Zaloguj się.', 'success');
          this.router.navigate(['/home']); // lub /login
        },
        error: (err) => {
          console.error(err);
          this.errorMessage.set('Wystąpił błąd podczas zmiany hasła. Link mógł wygasnąć.');
        }
      });
  }
}
