import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { AuthService } from './auth-service';
import { UserService } from './user-service';
import { environment } from '../../../environment';
import { Client, IFrame } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private readonly SERVER_URL = environment.serverUrl + '/ws';

  private stompClient: Client | null = null;

  public notificationSubject = new Subject<any>();
  public isConnected$ = new BehaviorSubject<boolean>(false);

  constructor(
    private authService: AuthService,
    private userService: UserService
  ) {}

  connect() {
    if (this.stompClient && this.stompClient.active) {
      return;
    }

    const token = this.authService.getAccessToken();

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(this.SERVER_URL),

      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },

      debug: () => null
    });

    this.stompClient.onConnect = (frame: IFrame) => {
      this.isConnected$.next(true);

      this.stompClient?.subscribe(
        `/user/${this.userService.getLoggedUserId()}/queue/notifications`,
        (msg) => {
          if (msg.body) {
            const notification = JSON.parse(msg.body);
            this.notificationSubject.next(notification);
          }
        }
      );
    };

    this.stompClient.onStompError = (frame) => {
      this.isConnected$.next(false);
    };

    this.stompClient.onWebSocketClose = () => {
      this.isConnected$.next(false);
    };

    this.stompClient.activate();
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
    this.isConnected$.next(false);
  }
}
