import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MyCookieService {

  check(name: string): boolean {
    return document.cookie.split(';').some((item) => item.trim().startsWith(name + '='));
  }

  set(name: string, value: string, days: number, path: string = '/'): void {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    const expires = "; expires=" + date.toUTCString();
    document.cookie = name + "=" + (value || "") + expires + "; path=" + path;
  }

  get(name: string): string | null {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for(let i=0; i < ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) == ' ') c = c.substring(1, c.length);
      if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
  }
}
