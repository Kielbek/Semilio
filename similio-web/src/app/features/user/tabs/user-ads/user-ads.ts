import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RouterLink} from '@angular/router';
import {LucideAngularModule} from 'lucide-angular';
import {EmptyState} from '../../../products/components/empty-state/empty-state';
import {MyProductCard} from '../../../products/components/my-product-card/my-product-card';
import {Card} from '../../../products/components/card/card';
import {List} from '../../../products/list/list';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-user-ads',
  imports: [
    RouterLink,
    LucideAngularModule,
    EmptyState,
    MyProductCard,
    Card,
    List
  ],
  templateUrl: './user-ads.html',
  styleUrl: './user-ads.css'
})
export class UserAds {
  @Input() productsSource?: (page: number, size: number) => Observable<any>;
  @Input() isMine = false;
  @Input() userId?: string;
  @Output() deleted = new EventEmitter<String>();

  onProductDeleted(id: string) {
    this.deleted.emit(id);
  }
}
