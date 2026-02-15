import {IImage} from './product/i-image';

export enum MessageState {
  SENT,
  SEEN
}

export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE'
}

export interface IMessage {
  id: number;
  content: string;
  mediaFile?: IImage;
  type: MessageType;
  state?: MessageState;
  senderId: string;
  receiverId?: string;
  chatId?: string;
  createdAt: Date;
  data?: Record<string, any>;
}
