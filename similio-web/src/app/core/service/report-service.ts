import {inject, Injectable} from '@angular/core';
import {environment} from '../../../environment';
import { HttpClient } from '@angular/common/http';
import {toSignal} from '@angular/core/rxjs-interop';
import {shareReplay} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private http = inject(HttpClient);

  private readonly baseUrl = environment.apiBase + '/reports';

  private categories$ = this.http.get<any[]>(`${this.baseUrl}/categories`).pipe(
    shareReplay(1)
  );

  public readonly categories = toSignal(this.categories$, { initialValue: [] });

  submitReport(request: any) {
    return this.http.post(`${this.baseUrl}`, request);
  }
}
