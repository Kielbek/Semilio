export interface IProductSearchCriteria {
  query?: string;
  minPrice?: number;
  maxPrice?: number;
  condition?: string;
  categoryId?: number;
  sizeId?: number;
  brandId?: number;
  colorId?: number;
  sort?: string;
}
