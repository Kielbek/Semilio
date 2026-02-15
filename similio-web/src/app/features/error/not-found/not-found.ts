import {Component} from '@angular/core';
import {AppConfig} from '../../../core/config/app-paths';
import {Button} from '../../../shared/button/button';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [
    Button,
    RouterLink,
  ],
  templateUrl: './not-found.html',
  styleUrl: './not-found.css',
})
export class NotFound {
  protected readonly paths = AppConfig;
}
