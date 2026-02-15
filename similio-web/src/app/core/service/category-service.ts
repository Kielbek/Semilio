import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {map, Observable, of} from 'rxjs';
import { ICategory } from '../models/i-category';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private jsonUrl = '/categories.json';

  constructor(private http: HttpClient) {}

  getCategories(): Observable<ICategory[]> {
    return this.http.get<ICategory[]>(this.jsonUrl);
  }

  getCategoryPath(targetId: number): Observable<ICategory[]> {
    return this.http.get<ICategory[]>(this.jsonUrl).pipe(
      map(categories => {
        const path: ICategory[] = [];
        this.findPath(categories, targetId, path);
        return path;
      })
    );
  }

  private findPath(categories: ICategory[], targetId: number, path: ICategory[]): boolean {
    for (const cat of categories) {
      path.push(cat);
      if (cat.id === targetId) return true;

      if (cat.subcategories && this.findPath(cat.subcategories, targetId, path)) {
        return true;
      }
      path.pop();
    }
    return false;
  }
}
