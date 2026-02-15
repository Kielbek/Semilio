import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LayoutService {
  private headerHeightSubject = new BehaviorSubject<number>(0);
  headerHeight$ = this.headerHeightSubject.asObservable();

  setHeaderHeight(height: number) {
    this.headerHeightSubject.next(height);
  }
}
