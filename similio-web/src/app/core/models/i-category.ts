export interface ICategory {
  id: number;
  name: string;
  icon?: string;
  subcategories?: ICategory[];
}
