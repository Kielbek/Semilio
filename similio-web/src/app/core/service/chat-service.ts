import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { environment } from '../../../environment';
import { IMessage, MessageState, MessageType } from '../models/i-message';
import { WebSocketService } from './web-socket-service';
import {IPage} from '../models/i-page';
import {IChat} from '../models/i-chat';

// DTO dla żądania wysłania wiadomości (zamiast any)
export interface SendMessageRequest {
  content: string;
  chatId?: string;
  productId?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private http = inject(HttpClient);
  private wsService = inject(WebSocketService);

  private readonly baseChatUrl = `${environment.apiBase}/chats`;
  private readonly baseMessageUrl = `${environment.apiBase}/messages`;

  // === STATE MANAGEMENT (Single Source of Truth) ===

  // 1. Lista czatów (dla ChatLayout / Sidebar)
  private chatsSubject = new BehaviorSubject<IChat[]>([]);
  public readonly chats$ = this.chatsSubject.asObservable();

  // 2. Wiadomości aktywnego czatu (dla ChatDetail)
  private activeMessagesSubject = new BehaviorSubject<IMessage[]>([]);
  public readonly activeMessages$ = this.activeMessagesSubject.asObservable();

  // 3. Stan wewnętrzny
  private currentActiveChatId: string | null = null;

  constructor() {
    this.initWebSocketListener();
  }

  // =================================================================
  // 1. WEBSOCKET & SYNC LOGIC (The Brain)
  // =================================================================

  private initWebSocketListener() {
    // Upewniamy się, że połączenie istnieje
    if (!this.wsService.isConnected$.value) {
      this.wsService.connect();
    }

    // Nasłuchujemy GLOBALNIE na wszystkie wiadomości
    this.wsService.notificationSubject.pipe(
      filter((n: any) => n && n.type === 'CHAT_MESSAGE')
    ).subscribe(notif => {
      this.handleRealTimeMessage(notif);
    });
  }

  private handleRealTimeMessage(notif: any) {
    const chatId = notif.data?.chatId || notif.chatId;

    // A. Aktualizacja Listy Czatów (Sidebar)
    // Przesuwamy czat na górę i aktualizujemy ostatnią wiadomość
    this.updateChatListState(chatId, notif);

    if (!chatId) return;

    // B. Aktualizacja Szczegółów (Detail)
    // Aktualizujemy tylko jeśli użytkownik patrzy na ten konkretny czat
    if (this.currentActiveChatId === chatId) {
      this.pushToActiveMessagesSafe(notif);
    }
  }

  private pushToActiveMessagesSafe(notif: any) {
    const msgId = Number(notif.id);
    const currentMsgs = this.activeMessagesSubject.value;

    // SAFETY NET: Sprawdzamy czy wiadomość już istnieje (rozwiązuje problem duplikatów)
    if (currentMsgs.some(m => m.id === msgId)) {
      return; // Ignorujemy duplikat
    }

    const newMsg = this.mapToIMessage(notif);
    this.activeMessagesSubject.next([...currentMsgs, newMsg]);
  }

  private updateChatListState(chatId: string, notif: any) {
    const currentChats = this.chatsSubject.value;
    const index = currentChats.findIndex(c => c.id === chatId);

    const lastMessageContent = notif.content ||
      (notif.data?.messageType === 'IMAGE' ? 'Przesłano zdjęcie' : 'Nowa wiadomość');

    if (index > -1) {
      const updatedChat = {
        ...currentChats[index],
        lastMessage: lastMessageContent,
        lastMessageDate: new Date(),
      };

      const otherChats = currentChats.filter(c => c.id !== chatId);
      this.chatsSubject.next([updatedChat, ...otherChats]);
    } else {
      this.getChatById(chatId).subscribe({
        next: (newChat) => {
          const freshList = this.chatsSubject.value;

          if (freshList.some(c => c.id === chatId)) return;

          const chatToAdd = {
            ...newChat,
            lastMessage: lastMessageContent,
            lastMessageDate: new Date().toISOString()
          };

          this.chatsSubject.next([chatToAdd, ...freshList]);
        },
        error: (err) => console.error('Błąd pobierania nowego czatu z WebSocket:', err)
      });
    }
  }

  // =================================================================
  // 2. STATE ACTIONS (Called by Components)
  // =================================================================

  /**
   * Wywoływane gdy użytkownik wchodzi do konkretnego czatu.
   * Resetuje stan wiadomości i ładuje nowe.
   */
  enterChat(chatId: string) {
    this.currentActiveChatId = chatId;
    this.activeMessagesSubject.next([]); // Czyścimy widok (lub pokazujemy loader)

    this.getMessagesApi(chatId).subscribe(page => {
      // Odwracamy kolejność jeśli backend zwraca od najnowszych, a frontend renderuje od dołu
      const sortedMessages = page.content.reverse(); // Zakładam, że mapowanie jest w getMessagesApi
      this.activeMessagesSubject.next(sortedMessages);
    });
  }

  /**
   * Wywoływane przy wyjściu z czatu (np. ngOnDestroy w Detail)
   */
  leaveChat() {
    this.currentActiveChatId = null;
    this.activeMessagesSubject.next([]);
  }

  // =================================================================
  // 3. HTTP REQUESTS (With State Updates)
  // =================================================================

  loadUserChats(page: number, size: number): Observable<IPage<IChat>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<IPage<IChat>>(this.baseChatUrl, { params }).pipe(
      tap(response => {
        // Dodajemy nowe czaty do istniejącej listy (dla paginacji "Load More")
        const current = this.chatsSubject.value;
        // W realnym świecie warto sprawdzić duplikaty ID przy łączeniu tablic
        this.chatsSubject.next([...current, ...response.content]);
      })
    );
  }

  loadOlderMessages(chatId: string, page: number): Observable<IPage<IMessage>> {
    return this.getMessagesApi(chatId, page).pipe(
      tap(response => {
        const currentMsgs = this.activeMessagesSubject.value;
        // API zwraca np. [msg20, msg19...], odwracamy żeby mieć chronologię
        const olderMsgs = response.content.reverse();

        // Zabezpieczenie przed duplikatami (gdyby coś się przesunęło na backendzie)
        const uniqueOlder = olderMsgs.filter(old => !currentMsgs.some(curr => curr.id === old.id));

        if (uniqueOlder.length > 0) {
          // PREPEND: [Starsze, ...Obecne]
          this.activeMessagesSubject.next([...uniqueOlder, ...currentMsgs]);
        }
      })
    );
  }

  sendMessage(request: SendMessageRequest): Observable<IMessage> {
    return this.http.post<any>(this.baseMessageUrl, request).pipe(
      map(dto => this.mapToIMessage(dto)),
      tap(savedMsg => {
        if (this.currentActiveChatId === savedMsg.chatId || this.currentActiveChatId === request.chatId) {
          const current = this.activeMessagesSubject.value;
          this.activeMessagesSubject.next([...current, savedMsg]);
        }
      })
    );
  }

  uploadMedia(chatId: string, file: File): Observable<IMessage> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<any>(`${this.baseMessageUrl}/${chatId}/media`, formData).pipe(
      map(dto => this.mapToIMessage(dto)),
      tap(savedMsg => {
        if (this.currentActiveChatId === chatId) {
          const current = this.activeMessagesSubject.value;
          this.activeMessagesSubject.next([...current, savedMsg]);
        }
      })
    );
  }

  clearChatsList() {
    this.chatsSubject.next([]); // Resetujemy tablicę do pusta
  }

  // =================================================================
  // 4. HELPERS & READ-ONLY CALLS
  // =================================================================

  private getMessagesApi(chatId: string, page: number = 0, size: number = 20): Observable<IPage<IMessage>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdDate,desc');

    return this.http.get<IPage<any>>(`${this.baseMessageUrl}/${chatId}`, { params });
  }

  getChatById(chatId: string): Observable<IChat> {
    return this.http.get<IChat>(`${this.baseChatUrl}/${chatId}`);
  }

  getChatByProductId(productId: string): Observable<IChat> {
    return this.http.get<IChat>(`${this.baseChatUrl}/product/${productId}`);
  }

  private mapToIMessage(dto: any): IMessage {
    return {
      id: Number(dto.id),
      content: dto.content || '',
      mediaFile: dto.data?.media || dto.mediaFile,
      type: dto.data?.messageType || dto.type || MessageType.TEXT,
      state: dto.state || MessageState.SENT,
      senderId: dto.data?.senderId || dto.senderId,
      chatId: dto.chatId,
      receiverId: dto.receiverId,
      createdAt: new Date(dto.createdAt || dto.createdDate),
      data: dto.data
    };
  }
}
