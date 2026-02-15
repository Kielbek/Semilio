import { Component } from '@angular/core';
import { IFooterLink } from '../../core/models/i-footer-link';
import { FooterColumn } from './footer-column/footer-column';
import { FooterSocial } from './footer-social/footer-social';
import {AppConfig} from '../../core/config/app-paths';

@Component({
  selector: 'app-footer',
  imports: [FooterColumn, FooterSocial],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
  standalone: true
})
export class Footer {
  protected readonly paths = AppConfig.LINKS;

  readonly aboutLinks: IFooterLink[] = [
    { href: AppConfig.LINKS.ABOUT.PLATFORM, label: 'Nasza Platforma' },
    { href: AppConfig.LINKS.ABOUT.SUSTAINABILITY, label: 'Zrównoważony rozwój' },
    { href: AppConfig.LINKS.ABOUT.PRESS, label: 'Materiały prasowe' },
  ];

  readonly helpLinks: IFooterLink[] = [
    { href: AppConfig.LINKS.HELP.MAIN, label: 'Centrum Pomocy' },
    { href: AppConfig.LINKS.HELP.SELLING, label: 'Sprzedawanie' },
    { href: AppConfig.LINKS.HELP.BUYING, label: 'Kupowanie' },
    { href: AppConfig.LINKS.HELP.SAFETY, label: 'Polityka bezpieczeństwa' },
  ];

  readonly legalLinks: IFooterLink[] = [
    { href: AppConfig.LINKS.LEGAL.TERMS, label: 'Regulamin' },
    { href: AppConfig.LINKS.LEGAL.COOKIES, label: 'Polityka Cookies' },
    { href: AppConfig.LINKS.LEGAL.PRIVACY, label: 'Centrum prywatności' },
  ];
}
