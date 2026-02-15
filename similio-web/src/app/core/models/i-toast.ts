export interface IToast {
  id: number;
  message: string;
  type: 'success' | 'error';
  visible: boolean
}
