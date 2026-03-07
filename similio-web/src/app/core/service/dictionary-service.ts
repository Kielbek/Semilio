import {inject, Injectable } from '@angular/core';
import {environment} from '../../../environment';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';
import { IDictionaryConfig } from '../models/dictionary/i-dictionary-config';

@Injectable({
  providedIn: 'root',
})
export class DictionaryService {
  private http = inject(HttpClient);
  private readonly baseUrl = environment.apiBase + '/dictionaries';

  public dictionaries$: Observable<IDictionaryConfig> =
    this.http.get<IDictionaryConfig>(`${this.baseUrl}/public/init`).pipe(
    shareReplay(1)
  );
}
