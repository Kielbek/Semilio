import {inject, Injectable} from '@angular/core';
import { WebSocketService } from './web-socket-service';
import {BehaviorSubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {UserService} from './user-service';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private wsService = inject(WebSocketService);
  private userService = inject(UserService)

  private hasUnreadChatMessagesSubject = new BehaviorSubject<boolean>(false);
  public readonly hasUnreadChatMessages$ = this.hasUnreadChatMessagesSubject.asObservable();

  constructor() {
    this.initGlobalListeners();
  }

  private initGlobalListeners() {
    this.wsService.notificationSubject.pipe(
      filter(n => !!n)
    ).subscribe(n => {
      switch (n.type) {
        case 'CHAT_MESSAGE':
          this.handleChatMessage(n);
          break;
        case 'OFFER_RECEIVED':
          break;
        case 'NOTIFICATION_READ':
          break;
      }
    });
  }

  private handleChatMessage(n: any) {
    if (n.payload.senderId === this.userService.getLoggedUserId()) {
      this.setUnreadChatMessages(true);
    }
  }

  public setUnreadChatMessages(status: boolean) {
    this.hasUnreadChatMessagesSubject.next(status);
  }
}
