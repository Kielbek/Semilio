import {Component, inject, Input, OnInit, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {FormSection} from '../../../../shared/form-section/form-section';
import {InputField} from '../../../../shared/input-field/input-field';
import {Button} from '../../../../shared/button/button';
import {CustomValidators} from '../../../../core/validators/custom-validators';
import {COUNTRIES} from '../../../../core/constants/countries';
import {IUser} from '../../../../core/models/i-user';
import {UserService} from '../../../../core/service/user-service';
import {ToastService} from '../../../../core/service/toast-service';

@Component({
  selector: 'app-account-settings',
  imports: [
    ReactiveFormsModule,
    FormSection,
    InputField,
    Button
  ],
  templateUrl: './account-settings.html',
  styleUrl: './account-settings.css'
})
export class AccountSettings implements OnInit {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private toastService = inject(ToastService);

  loading = signal(false);

  user: IUser | null = null;

  infoForm!: FormGroup;
  passwordForm!: FormGroup;

  readonly countries = COUNTRIES;

  ngOnInit(): void {
    this.user = this.userService.getUser();

    this.initForm();
  }

  initForm() {
    const fullPhone = this.user?.phoneNumber || '';

    const foundCountry = COUNTRIES.find(c => fullPhone.startsWith(c.code)) || { code: '+48' };

    const phoneWithoutCode = fullPhone.replace(foundCountry.code, '');

    this.infoForm = this.fb.group({
      email: [this.user?.email, [Validators.required, Validators.email]],
      countryCode: [foundCountry.code],
      phone: [phoneWithoutCode, [Validators.required, Validators.pattern('^[0-9]+$')]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: CustomValidators.match('newPassword', 'confirmPassword'),
    });
  }

  getControl(form: FormGroup, name: string) {
    return form.get(name) as any;
  }

  onInfoSubmit() {
    if (this.infoForm.invalid) return;

    this.loading.set(true);

    setTimeout(() => {
      this.loading.set(false);
    }, 1000);
  }

  onPasswordSubmit() {
    if (this.passwordForm.invalid) return;

    this.loading.set(true);

    const formValue = this.passwordForm.getRawValue();

    this.userService.changePassword(formValue.currentPassword, formValue.newPassword, formValue.confirmPassword).subscribe({
      next: () => {
        this.toastService.show('Hasło zostało zmienione', 'success');
        this.passwordForm.reset();
        this.loading.set(false);
      },
      error: (err) => {
        this.toastService.show(err.error?.message || 'Błąd zmiany hasła', 'error');
        this.loading.set(false);
      }
    });
  }

  onDeleteAccount() {
    if(confirm('Czy na pewno chcesz usunąć konto? Tej operacji nie można cofnąć.')) {
      console.log('Rozpoczęto procedurę usuwania konta');
    }
  }
}
