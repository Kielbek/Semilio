import {Component, inject} from '@angular/core';
import {FavoriteService} from '../../core/service/favorite-service';
import {Card} from '../products/components/card/card';
import {List} from '../products/list/list';
import {EmptyState} from '../products/components/empty-state/empty-state';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-favorite',
  imports: [
    Card,
    List,
    EmptyState,
    RouterLink
  ],
  templateUrl: './favorite.html',
  styleUrl: './favorite.css'
})
export class Favorite {
  private favoriteService = inject(FavoriteService);

  favoritesSource = (page: number, size: number) => {
    return this.favoriteService.getUserFavorites(page, size);
  };
}
