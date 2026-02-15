export enum Condition {
  NEW_WITH_TAGS = 'NEW_WITH_TAGS',
  NEW_WITHOUT_TAGS = 'NEW_WITHOUT_TAGS',
  VERY_GOOD = 'VERY_GOOD',
  GOOD = 'GOOD',
  SATISFACTORY = 'SATISFACTORY'
}

export const ConditionDisplay: Record<Condition, string> = {
  [Condition.NEW_WITH_TAGS]: 'Nowy z metką',
  [Condition.NEW_WITHOUT_TAGS]: 'Nowy bez metki',
  [Condition.VERY_GOOD]: 'Bardzo dobry',
  [Condition.GOOD]: 'Dobry',
  [Condition.SATISFACTORY]: 'Zadowalający'
};

export const ConditionDescriptions: Record<Condition, string> = {
  [Condition.NEW_WITH_TAGS]: 'Nowy, nieużywany przedmiot z dołączonymi metkami lub w oryginalnym opakowaniu.',
  [Condition.NEW_WITHOUT_TAGS]: 'Nowy, nieużywany przedmiot bez metek lub oryginalnego opakowania.',
  [Condition.VERY_GOOD]: 'Przedmiot używany, ale w świetnym stanie, z minimalnymi śladami użytkowania.',
  [Condition.GOOD]: 'Przedmiot używany, nosi normalne ślady użytkowania, ale jest w pełni sprawny.',
  [Condition.SATISFACTORY]: 'Przedmiot często używany, może mieć widoczne wady, które są opisane.'
};

export interface ConditionOption {
  label: string;
  value: Condition;
  description: string;
}

export const CONDITION_OPTIONS: ConditionOption[] = Object.values(Condition).map(key => ({
  label: ConditionDisplay[key],
  value: key,
  description: ConditionDescriptions[key]
}));
