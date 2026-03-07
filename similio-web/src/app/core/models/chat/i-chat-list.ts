import {IImage} from '../product/i-image';

export interface IChatList {
  id: string;

  interlocutorName: string;
  interlocutorImage: string | null;

  productTitle: string;
  productId: string;
  productImage: IImage;

  lastMessage: string | null;
  lastMessageDate: string | Date;

  unreadCount: number;
}
