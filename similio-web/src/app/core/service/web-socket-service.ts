import {Injectable} from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {AuthService} from './auth-service';
import SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';
import {UserService} from './user-service';
import {environment} from '../../../environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private readonly SERVER_URL = environment.serverUrl + '/ws';

  private stompClient: any;

  public notificationSubject = new Subject<any>();

  public isConnected$ = new BehaviorSubject<boolean>(false);

  constructor(
    private authService: AuthService,
    private userService: UserService
  ) {}

  connect() {
    const socket = new SockJS(this.SERVER_URL);
    this.stompClient = Stomp.over(socket);

    this.stompClient.debug = null;

    const token = this.authService.getAccessToken();

    this.stompClient.connect(
      { 'Authorization': `Bearer ${token}` },
      (frame: any) => {
        this.isConnected$.next(true);

        this.stompClient.subscribe(`/user/${this.userService.getLoggedUserId()}/queue/notifications`, (msg: any) => {
          if (msg.body) {
            const notification = JSON.parse(msg.body);
            this.notificationSubject.next(notification);
          }
        });
      },
    );
  }

  disconnect() {
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
    }
    this.isConnected$.next(false);
  }


}
