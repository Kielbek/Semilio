import { Component, Input } from '@angular/core';
import {ISeller} from '../../../../core/models/product/i-seller';
import {UserAvatar} from '../../../../shared/user-avatar/user-avatar';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-seller-card',
  imports: [
    UserAvatar,
    RouterLink
  ],
  templateUrl: './seller-card.html',
  styleUrl: './seller-card.css',
})
export class SellerCard {
  @Input({ required: true }) seller?: ISeller;
}
