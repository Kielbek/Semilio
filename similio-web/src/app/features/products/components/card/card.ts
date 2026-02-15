import {Component, inject, Input} from '@angular/core';
import {Spinner} from '../../../../shared/spinner/spinner';
import {IProductCard} from '../../../../core/models/product/i-product-card';
import {FavoriteService} from '../../../../core/service/favorite-service';
import {RouterLink} from '@angular/router';
import {ListStateService} from '../../../../core/service/list-state-service';
import {DecimalPipe} from '@angular/common';
import {ConditionLabelPipe} from '../../../../core/pipes/condition-label-pipe';
import {AppConfig} from '../../../../core/config/app-paths';

@Component({
  selector: 'app-card',
  imports: [
    Spinner,
    RouterLink,
    DecimalPipe,
    ConditionLabelPipe
  ],
  templateUrl: './card.html',
  styleUrl: './card.css',
})
export class Card {
  private favoriteService = inject(FavoriteService);
  private listStateService = inject(ListStateService);
  protected readonly path = AppConfig;

  @Input({ required: true }) product!: IProductCard;
  loaded = false;

  onLoad() {
    this.loaded = true;
  }

  onError() {
    this.loaded = true;
  }

  onToggleFavorite(event: Event) {
    event.stopPropagation();
    event.preventDefault();

    const wasLiked = this.product.likedByCurrentUser;

    this.product.likedByCurrentUser = !wasLiked;

    if (wasLiked) {
      this.product.stats.likes--;
    } else {
      this.product.stats.likes++;
    }

    this.favoriteService.toggleFavorite(this.product.id).subscribe({
      next: () => {
        this.listStateService.clearState('my-favorites');
      },
      error: () => {
        this.product.likedByCurrentUser = wasLiked;
        this.product.stats.likes = wasLiked ? this.product.stats.likes + 1 : this.product.stats.likes - 1;
      }
    });
  }
}
