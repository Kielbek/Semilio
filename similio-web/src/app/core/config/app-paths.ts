const S = {
  HOME: '',
  CHAT: 'chat',
  SETTINGS: 'settings',
  PROFILE: 'profile',
  PRODUCT: 'product',
  SEARCH: 'search',
  AUTH: 'auth',

  INFO: 'info',
  ABOUT: 'about',
  HELP: 'help',
  LEGAL: 'legal',

  CREATE: 'create-product',
  EDIT: 'edit-product',
  DETAILS: 'product-details',
  SUCCESS: 'create-success',
  FAVORITE: 'favorite',
  RESET_PASS: 'reset-password',

  PLATFORM: 'platform',
  SUSTAINABILITY: 'sustainability',
  PRESS: 'press',
  SELLING: 'selling',
  BUYING: 'buying',
  SAFETY: 'safety',
  TERMS: 'terms',
  COOKIES: 'cookies',
  PRIVACY: 'privacy',
  CENTER: 'center',

  ACCOUNT: 'account',
  ADS: 'ads',
  RATINGS: 'ratings',

  ERROR: 'error',
  NOT_FOUND: '404'
} as const;

export const AppConfig = {
  SEGMENTS: S,

  LINKS: {
    HOME: '/',
    RESET_PASSWORD: `/${S.RESET_PASS}`,

    PRODUCT: {
      SEARCH: `/${S.SEARCH}`,
      CREATE: `/${S.CREATE}`,
      FAVORITE: `/${S.FAVORITE}`,
      EDIT_ROOT: `/${S.EDIT}`,
      DETAILS: `/${S.DETAILS}`,
    },

    CHAT: `/${S.CHAT}`,

    SETTINGS: {
      ROOT: `/${S.SETTINGS}`,
      PROFILE: `/${S.SETTINGS}/${S.PROFILE}`,
      ACCOUNT: `/${S.SETTINGS}/${S.ACCOUNT}`,
      PRIVACY: `/${S.SETTINGS}/${S.PRIVACY}`,
    },

    ABOUT: {
      PLATFORM: `/${S.ABOUT}/${S.PLATFORM}`,
      PRESS: `/${S.ABOUT}/${S.PRESS}`,
      SUSTAINABILITY: `/${S.ABOUT}/${S.SUSTAINABILITY}`,
    },
    HELP: {
      MAIN: `/${S.HELP}`,
      SELLING: `/${S.HELP}/${S.SELLING}`,
      BUYING: `/${S.HELP}/${S.BUYING}`,
      SAFETY: `/${S.HELP}/${S.SAFETY}`,
    },
    LEGAL: {
      TERMS: `/${S.LEGAL}/${S.TERMS}`,
      PRIVACY: `/${S.LEGAL}/${S.PRIVACY}`,
      COOKIES: `/${S.LEGAL}/${S.COOKIES}`,
    }
  }
} as const;
