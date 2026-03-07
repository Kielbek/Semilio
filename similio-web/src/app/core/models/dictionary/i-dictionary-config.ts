import {IDictionaryItem} from './i-dictionary-item';

export interface IDictionaryConfig {
  brands: IDictionaryItem[];
  colors: IDictionaryItem[];
  sizes: Record<string, IDictionaryItem[]>;
}
