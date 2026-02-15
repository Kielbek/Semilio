import { Component, Input } from '@angular/core';
import { IFooterLink } from '../../../core/models/i-footer-link';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-footer-column',
  imports: [
    RouterLink
  ],
  templateUrl: './footer-column.html',
  styleUrl: './footer-column.css',
})
export class FooterColumn {
  @Input() title!: string;
  @Input() links: IFooterLink[] = [];
}
