import {Component, ElementRef, EventEmitter, Output, QueryList, ViewChildren} from '@angular/core';
import {AuthService} from '../../../../core/service/auth-service';
import { NotificationService } from '../../../../core/service/notification';
import {ToastService} from '../../../../core/service/toast-service';

@Component({
  selector: 'app-sms-verification',
  imports: [],
  templateUrl: './sms-verification.html',
  styleUrl: './sms-verification.css'
})
export class SmsVerification {

  @ViewChildren('otpInput') inputs!: QueryList<ElementRef>;
  @Output() verificationSuccess = new EventEmitter<void>();

  loading = false;
  otpCode: string[] = ['', '', '', '', '', ''];
  successMessage = '';
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private toastService: ToastService,
  ) {}

  onInput(event: any, index: number) {
    const val = event.target.value;

    if (!/^\d$/.test(val)) {
      event.target.value = '';
      return;
    }

    this.otpCode[index] = val;

    if (index < 5 && val) {
      this.inputs.toArray()[index + 1].nativeElement.focus();
    }
  }

  onKeyDown(event: KeyboardEvent, index: number) {
    if (event.key === 'Backspace') {
      if (!this.otpCode[index] && index > 0) {
        this.inputs.toArray()[index - 1].nativeElement.focus();
      }
      this.otpCode[index] = '';
    }
  }

  onPaste(event: ClipboardEvent) {
    const data = event.clipboardData?.getData('text');
    if (data && data.length === 6 && /^\d+$/.test(data)) {
      const chars = data.split('');
      this.inputs.forEach((input, i) => {
        input.nativeElement.value = chars[i];
        this.otpCode[i] = chars[i];
      });
      this.inputs.toArray()[5].nativeElement.focus();
    }
  }

  isCodeComplete(): boolean {
    return this.otpCode.every(digit => digit !== '');
  }

  verifyCode() {
    if (!this.isCodeComplete()) {
      this.errorMessage = 'Please enter the full 6-digit code';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const finalCode = Number(this.otpCode.join(''));

    this.authService.verifySms(finalCode).subscribe({
      next: () => {
        this.toastService.show('Konto utworzone! Możesz się teraz zalogować.', 'success');
        this.verificationSuccess.emit();
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Verification failed';
        this.loading = false;
      }
    });
  }

  resendCode() {
    console.log('Wysyłam kod ponownie...');
  }
}
