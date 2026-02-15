export enum Color {
  BLACK = 'BLACK',
  WHITE = 'WHITE',
  RED = 'RED',
  BLUE = 'BLUE',
  GREEN = 'GREEN',
  YELLOW = 'YELLOW',
  GRAY = 'GRAY',
  BEIGE = 'BEIGE',
  MULTICOLOR = 'MULTICOLOR'
}

export const ColorDisplay: Record<Color, string> = {
  [Color.BLACK]: 'Czarny',
  [Color.WHITE]: 'Biały',
  [Color.RED]: 'Czerwony',
  [Color.BLUE]: 'Niebieski',
  [Color.GREEN]: 'Zielony',
  [Color.YELLOW]: 'Żółty',
  [Color.GRAY]: 'Szary',
  [Color.BEIGE]: 'Beżowy',
  [Color.MULTICOLOR]: 'Wielokolorowy'
};

// Mapowanie na kody HEX (potrzebne do kółeczek w UI)
export const ColorHex: Record<Color, string> = {
  [Color.BLACK]: '#000000',
  [Color.WHITE]: '#FFFFFF',
  [Color.RED]: '#EF4444',
  [Color.BLUE]: '#3B82F6',
  [Color.GREEN]: '#22C55E',
  [Color.YELLOW]: '#EAB308',
  [Color.GRAY]: '#6B7280',
  [Color.BEIGE]: '#F5F5DC',
  [Color.MULTICOLOR]: 'linear-gradient(to right, red, orange, yellow, green, blue, indigo, violet)'
};

export interface ColorOption {
  label: string;
  value: Color;
  hex: string;
}

export const COLOR_OPTIONS: ColorOption[] = Object.values(Color).map(key => ({
  label: ColorDisplay[key],
  value: key,
  hex: ColorHex[key]
}));
