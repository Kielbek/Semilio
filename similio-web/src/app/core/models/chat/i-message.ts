export enum MessageState {
  SENT,
  SEEN
}

export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  PROPOSAL = 'PROPOSAL'
}

export enum ProposalStatus {
  PENDING = 'PENDING',
  SUGGESTED = 'SUGGESTED',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED'
}

export interface BaseMessagePayload {
  type: MessageType;
}

export interface TextMessagePayload extends BaseMessagePayload {
  type: MessageType.TEXT;
  text: string;
}

export interface ImageMessagePayload extends BaseMessagePayload {
  type: MessageType.IMAGE;
  url: string;
  width?: number;
  height?: number;
}

export interface ProposalMessagePayload extends BaseMessagePayload {
  type: MessageType.PROPOSAL;
  productId: string;
  amount: number;
  originalPrice: number;
  currency: string;
  status: ProposalStatus
}

export type MessagePayload =
  | TextMessagePayload
  | ImageMessagePayload
  | ProposalMessagePayload;

export interface IMessage {
  id: string;
  chatId: string;
  senderId: string;
  createdAt: Date;
  state: MessageState;

  payload: MessagePayload;
}
