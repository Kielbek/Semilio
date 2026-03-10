import {Component, inject, signal} from '@angular/core';
import { CookieConsentService } from '../../core/service/cookie-consent-service';
import {CookieConsentOptions, DEFAULT_CONSENT} from '../../core/models/cookie-consent.model';
import {Button} from '../button/button';

@Component({
  selector: 'app-cookie-banner',
  imports: [
    Button
  ],
  templateUrl: './cookie-banner.html',
  styleUrl: './cookie-banner.css',
})
export class CookieBanner {
  private consentService = inject(CookieConsentService);

  showDetails = signal<boolean>(false);

  // Lokalne stany dla zaawansowanego widoku
  analyticsEnabled = signal<boolean>(false);
  adsEnabled = signal<boolean>(false);

  toggleDetails() {
    this.showDetails.set(!this.showDetails());
  }

  acceptAll() {
    this.consentService.acceptAll();
  }

  rejectAll() {
    this.consentService.rejectAll();
  }

  savePreferences() {
    const customOptions: CookieConsentOptions = {
      ...DEFAULT_CONSENT,
      analytics_storage: this.analyticsEnabled() ? 'granted' : 'denied',
      ad_storage: this.adsEnabled() ? 'granted' : 'denied',
      ad_user_data: this.adsEnabled() ? 'granted' : 'denied',
      ad_personalization: this.adsEnabled() ? 'granted' : 'denied'
    };
    this.consentService.saveCustom(customOptions);
  }
}
