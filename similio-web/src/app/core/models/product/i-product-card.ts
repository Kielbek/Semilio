import {IStats} from './i-stats';
import {IPrice} from './i-price';
import {Condition} from './condition';
import {IImage} from './i-image';
import {Status} from './status';

export interface IProductCard {
  id: string;
  title: string;
  slug: string;
  mainImage: IImage;
  price: IPrice;
  status: Status;
  size: string;
  condition: Condition;
  likedByCurrentUser: boolean;
  stats: IStats
}
