import { Observable } from 'rxjs';
import { IProductCard } from '../models/product/i-product-card';
import {IPage} from '../models/i-page';

export type FetchFunction = (page: number, size: number) => Observable<IPage<IProductCard>>;
