import {IPrice} from './product/i-price';
import {IImage} from './product/i-image';

export interface IChat {
  id: string;
  chatName?: string;
  productId?: string;
  productTitle: string;
  productPrice?: IPrice;

  productImage: IImage;

  lastMessage?: string;
  lastMessageDate?: string | Date;
  unreadCount?: number;

  recipientId?: string;
}
