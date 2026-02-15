import {inject, Injectable} from '@angular/core';
import {environment} from '../../../environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Observable} from 'rxjs';
import {IPage} from '../models/i-page';
import {IProductCard} from '../models/product/i-product-card';

@Injectable({
  providedIn: 'root'
})
export class FavoriteService {
  private http = inject(HttpClient);

  private readonly baseUrl = environment.apiBase + '/favorite';

  toggleFavorite(productId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${productId}`, {});
  }

  getUserFavorites(page: number, size: number): Observable<IPage<IProductCard>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<IPage<IProductCard>>(`${this.baseUrl}`, { params });
  }
}
