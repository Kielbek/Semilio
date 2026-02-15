export interface IPage<T> {
  content: T[];
  last: boolean;
  totalElements: number;
  number: number;
}
