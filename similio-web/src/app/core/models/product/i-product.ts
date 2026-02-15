import { IPrice } from './i-price';
import { IStats } from './i-stats';
import { Condition } from './condition';
import {IImage} from './i-image';

export interface IProduct {
  id: string;
  title: string;
  slug: string
  description: string;
  price: IPrice;
  size: string;
  condition: Condition;
  brand: string;
  color: string;
  categoryId: number;
  sellerId: number;
  images: IImage[];
  stats: IStats;
  createdAt: string | Date;
  seller?: any;
}
