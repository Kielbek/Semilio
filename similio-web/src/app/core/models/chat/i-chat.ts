import {IImage} from '../product/i-image';
import {IPrice} from '../product/i-price';
import {Status} from '../product/status';

export interface IChat {
  id: string;

  chatName?: string;
  productId: string;
  productTitle: string;

  productPrice: IPrice;
  productImage: IImage;
  productStatus?: Status;

  recipientId?: string;
}
