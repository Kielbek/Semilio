import {Injectable} from '@angular/core';
import {IProductCard} from '../models/product/i-product-card';
import {IProduct} from '../models/product/i-product';
import {environment} from '../../../environment';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import { IPage } from '../models/i-page';
import {IProductSearchCriteria} from '../models/i-product-search-criteria';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly baseUrl = environment.apiBase + '/products';

  constructor(private http: HttpClient) {}

  createProduct(dto: any, images: File[]): Observable<any> {
    const formData = new FormData();

    const jsonBlob = new Blob([JSON.stringify(dto)], {
      type: 'application/json'
    });

    formData.append('dto', jsonBlob);

    images.forEach(image => {
      formData.append('images', image);
    });

    return this.http.post(`${this.baseUrl}`, formData);
  }

  getProductById(id: string): Observable<IProduct> {
    return this.http.get<IProduct>(`${this.baseUrl}/public/${id}`);
  }

  getProductBySlug(slug: string): Observable<IProduct> {
    return this.http.get<IProduct>(`${this.baseUrl}/public/items/${slug}`);
  }

  getUserProducts(page: number, size: number = 10): Observable<IPage<IProductCard>> {

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdDate,desc');

    return this.http.get<IPage<IProductCard>>(`${this.baseUrl}/user`, { params });
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  toggleVisibility(id: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/visibility`, {});
  }

  updateProduct(productId: string, dto: any, images: File[]) {
    const formData = new FormData();

    const jsonBlob = new Blob([JSON.stringify(dto)], {
      type: 'application/json'
    });

    formData.append('dto', jsonBlob);

    images.forEach(image => {
      formData.append('newFiles', image);
    });

    return this.http.put(`${this.baseUrl}/${productId}`, formData);
  }

  getFeaturedProducts(page: number, size: number, seed: string): Observable<IPage<IProductCard>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('seed', seed);

    return this.http.get<IPage<IProductCard>>(`${this.baseUrl}/public/featured`, { params });
  }

  search(criteria: IProductSearchCriteria, page: number = 0, size: number = 20): Observable<IPage<IProductCard>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (criteria.query) params = params.set('query', criteria.query);
    if (criteria.minPrice) params = params.set('minPrice', criteria.minPrice.toString());
    if (criteria.maxPrice) params = params.set('maxPrice', criteria.maxPrice.toString());
    if (criteria.category) params = params.set('category', criteria.category);
    if (criteria.condition) params = params.set('condition', criteria.condition);
    if (criteria.size) params = params.set('productSize', criteria.size);

    return this.http.get<IPage<IProductCard>>(`${this.baseUrl}/public/search`, { params });
  }

  getSellerProducts(sellerId: string, page: number = 0, size: number = 10): Observable<IPage<IProductCard>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdDate,desc');

    return this.http.get<IPage<IProductCard>>(`${this.baseUrl}/public/seller/${sellerId}`, { params });
  }

  incrementView(id: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/public/${id}/view`, {});
  }
}
