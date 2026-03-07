export interface ICategory {
  id: number;
  name: string;
  icon?: string;
  sizeType?: string;
  subcategories?: ICategory[];
}
