import { Injectable } from '@angular/core';

export interface ListState {
  products: any[];
  currentPage: number;
  isLastPage: boolean;
  scrollPosition: number;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class ListStateService {
  private cache = new Map<string, ListState>();

  saveState(key: string, state: Omit<ListState, 'timestamp'>): void {
    this.cache.set(key, {
      ...state,
      timestamp: Date.now()
    });
  }

  getState(key: string): ListState | undefined {
    const state = this.cache.get(key);

    if (!state) {
      return undefined;
    }

    if (Date.now() - state.timestamp > 10 * 60 * 1000) {
      this.cache.delete(key);
      return undefined;
    }

    return state;
  }

  clearState(key: string): void {
    this.cache.delete(key);
  }
}
