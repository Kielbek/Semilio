import { Injectable, signal } from '@angular/core';
import {CookieConsentOptions, DEFAULT_CONSENT} from '../models/cookie-consent.model';

declare global {
  interface Window {
    dataLayer: any[];
    gtag: (...args: any[]) => void;
  }
}

@Injectable({
  providedIn: 'root',
})
export class CookieConsentService {
  private readonly STORAGE_KEY = 'semilio_cookie_consent';

  // Signal przechowujący obecny stan (czy w ogóle pokazano baner)
  isBannerVisible = signal<boolean>(true);

  // Signal przechowujący aktualne zgody
  preferences = signal<CookieConsentOptions>(DEFAULT_CONSENT);

  constructor() {
    this.initConsent();
  }

  private initConsent() {
    const saved = localStorage.getItem(this.STORAGE_KEY);

    if (saved) {
      this.isBannerVisible.set(false);
      const parsed = JSON.parse(saved) as CookieConsentOptions;
      this.preferences.set(parsed);
      this.updateGtagConsent(parsed);
    } else {
      // Brak decyzji – ustawiamy domyślne (denied)
      this.updateGtagConsent(DEFAULT_CONSENT);
    }
  }

  // Użytkownik klika "Akceptuj wszystko"
  acceptAll() {
    const allGranted: CookieConsentOptions = {
      analytics_storage: 'granted',
      ad_storage: 'granted',
      ad_user_data: 'granted',
      ad_personalization: 'granted'
    };
    this.saveConsent(allGranted);
  }

  // Użytkownik klika "Odrzuć wszystko" (Wymóg RODO - musi być równie łatwe co akceptacja)
  rejectAll() {
    this.saveConsent(DEFAULT_CONSENT);
  }

  // Użytkownik klika "Zapisz preferencje" po wybraniu opcji
  saveCustom(customPreferences: CookieConsentOptions) {
    this.saveConsent(customPreferences);
  }

  private saveConsent(options: CookieConsentOptions) {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(options));
    this.preferences.set(options);
    this.isBannerVisible.set(false);

    // Aktualizujemy Google Tag Managera o nowej decyzji
    this.updateGtagConsent(options);
  }

  private updateGtagConsent(options: CookieConsentOptions) {
    if (typeof window !== 'undefined' && window.gtag) {
      window.gtag('consent', 'update', options);
      // Dodatkowy event, jeśli GTM czeka na trigger
      window.dataLayer = window.dataLayer || [];
      window.dataLayer.push({ event: 'cookie_consent_update' });
    }
  }
}
