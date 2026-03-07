import {IPrice} from './i-price';
import {IStats} from './i-stats';
import {IImage} from './i-image';
import {IDictionaryItem} from '../dictionary/i-dictionary-item';
import {Condition} from './condition';

export interface IProduct {
  id: string;
  title: string;
  slug: string;
  description: string;
  price: IPrice;
  condition: Condition;
  size: IDictionaryItem;
  brand: IDictionaryItem;
  color: IDictionaryItem;
  categoryId: number;
  sellerId: number;
  images: IImage[];
  stats: IStats;
  createdAt: string | Date;
  seller?: any;
}
