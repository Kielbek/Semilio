import {inject, Injectable } from '@angular/core';
import {MyCookieService} from './my-cookie-service';

@Injectable({
  providedIn: 'root'
})
export class ViewTrackerService {
  private cookie = inject(MyCookieService);
  private readonly PREFIX = 'prod_v_';

  canTrackView(productId: string): boolean {
    const name = `${this.PREFIX}${productId}`;

    if (this.cookie.check(name)) {
      return false;
    }

    this.cookie.set(name, '1', 1);
    return true;
  }
}
