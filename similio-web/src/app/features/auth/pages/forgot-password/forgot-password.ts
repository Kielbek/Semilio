import { ChangeDetectionStrategy, Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormControl, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { AuthService } from '../../../../core/service/auth-service';
import { Button } from '../../../../shared/button/button';
import { InputField } from '../../../../shared/input-field/input-field';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    Button,
    InputField
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ForgotPassword {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);

  @Output() loginClick = new EventEmitter<void>();

  readonly isLoading = signal(false);
  readonly isEmailSent = signal(false);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  getControl(name: string): FormControl {
    return this.form.get(name) as FormControl;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const { email } = this.form.getRawValue();

    this.authService.forgotPassword(email)
      .pipe(
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
        next: () => this.isEmailSent.set(true),
        error: (err) => {
          console.warn('Błąd resetowania hasła (ukryty dla użytkownika):', err);
          this.isEmailSent.set(true);
        }
      });
  }
}
