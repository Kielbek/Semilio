export interface INotification {
  id: number;
  type: string;
  text: string;
  createdAt: Date;
  read: boolean;
}
