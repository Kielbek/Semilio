import {Component} from '@angular/core';
import {Popup} from "../../../../shared/popup/popup";
import {SmsVerification} from '../sms-verification/sms-verification';
import {Register} from '../register/register';
import {ForgotPassword} from '../forgot-password/forgot-password';
import {Login} from '../login/login';
import {AuthService} from '../../../../core/service/auth-service';

@Component({
  selector: 'app-auth',
  imports: [
    Popup,
    Register,
    Login,
    ForgotPassword,
    SmsVerification
  ],
  templateUrl: './auth.html',
  styleUrl: './auth.css'
})
export class Auth {
  isLoginPopupOpen = false;
  mode: 'login' | 'register' | 'forgot-password' | 'sms-verification' = 'login';

  constructor(
    private authService: AuthService,
  ) {
    this.authService.showLoginPopup$.subscribe(open => {
      this.isLoginPopupOpen = open;
    });
  }

  closePopup() {
    this.isLoginPopupOpen = false;
    this.switchToLogin();
  }

  switchToLogin() {
    this.mode = 'login';
  }

  switchToRegister() {
    this.mode = 'register';
  }

  switchToForgotPassword() {
    this.mode = 'forgot-password';
  }

  switchToSmsVerification() {
    this.mode = 'sms-verification';
  }

}
