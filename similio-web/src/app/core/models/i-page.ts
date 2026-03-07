export interface IPageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface IPage<T> {
  content: T[];
  page: IPageMetadata;
}
