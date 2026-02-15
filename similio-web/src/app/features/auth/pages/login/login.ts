import {ChangeDetectionStrategy, Component, EventEmitter, inject, Output, signal} from '@angular/core';
import {FormControl, NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {finalize} from 'rxjs';

import {AuthService} from '../../../../core/service/auth-service';
import {Button} from '../../../../shared/button/button';
import {InputField} from '../../../../shared/input-field/input-field';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    Button,
    InputField
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Login {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);

  @Output() registerClick = new EventEmitter<void>();
  @Output() forgotPasswordClick = new EventEmitter<void>();

  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
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
    this.errorMessage.set(null);

    const { email, password } = this.form.getRawValue();

    this.authService.login(email, password)
      .pipe(
        finalize(() => this.isLoading.set(false))
      )
      .subscribe({
        next: () => {
        },
        error: (err) => {
          console.error(err);
          this.errorMessage.set('Nieprawidłowy adres e-mail lub hasło.');
        },
      });
  }
}
