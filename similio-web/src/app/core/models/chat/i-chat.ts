import {IImage} from '../product/i-image';
import {IPrice} from '../product/i-price';

export interface IChat {
  id: string;

  chatName?: string;
  productId: string;
  productTitle: string;

  productPrice: IPrice;
  productImage: IImage;

  recipientId?: string;
}
