export type ConsentStatus = 'granted' | 'denied';

export interface CookieConsentOptions {
  analytics_storage: ConsentStatus;
  ad_storage: ConsentStatus;
  ad_user_data: ConsentStatus;
  ad_personalization: ConsentStatus;
}

export const DEFAULT_CONSENT: CookieConsentOptions = {
  analytics_storage: 'denied',
  ad_storage: 'denied',
  ad_user_data: 'denied',
  ad_personalization: 'denied'
};
