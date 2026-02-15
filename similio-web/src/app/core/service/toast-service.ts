import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {IToast} from '../models/i-toast';

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<IToast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  show(message: string, type: IToast['type'] = 'success') {
    const id = Date.now();
    const newToast: IToast = { id, message, type, visible: false };

    this.toastsSubject.next([...this.toastsSubject.value, newToast]);

    setTimeout(() => {
      this.updateToast(id, { visible: true });
    }, 10);

    setTimeout(() => this.hideAndRemove(id), 3000);
  }

  hideAndRemove(id: number) {
    this.updateToast(id, { visible: false });

    setTimeout(() => {
      this.toastsSubject.next(this.toastsSubject.value.filter(t => t.id !== id));
    }, 300);
  }

  private updateToast(id: number, data: Partial<IToast>) {
    const updated = this.toastsSubject.value.map(t =>
      t.id === id ? { ...t, ...data } : t
    );
    this.toastsSubject.next(updated);
  }
}
