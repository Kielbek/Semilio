import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class TimeService {
  timeAgo(value: Date | string): string {
    const date = new Date(value);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    const weeks = Math.floor(days / 7);
    const months = Math.floor(days / 30);

    if (minutes < 1) return 'przed chwilą';
    if (minutes < 60)
      return `${minutes} ${minutes === 1 ? 'minuta' : 'minuty'} temu`;
    if (hours < 24)
      return `${hours} ${hours === 1 ? 'godzina' : 'godziny'} temu`;
    if (days < 7) return `${days} ${days === 1 ? 'dzień' : 'dni'} temu`;
    if (weeks < 4)
      return `${weeks} ${weeks === 1 ? 'tydzień' : 'tygodnie'} temu`;
    if (months < 12)
      return `${months} ${months === 1 ? 'miesiąc' : 'miesiące'} temu`;

    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  }
}
