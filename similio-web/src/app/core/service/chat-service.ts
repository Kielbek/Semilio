import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import {environment} from '../../../environment';
import {IMessage, MessageState, MessageType, ProposalStatus} from '../models/chat/i-message';
import {WebSocketService} from './web-socket-service';
import {IPage} from '../models/i-page';
import {IChat} from '../models/chat/i-chat';
import {IChatList} from '../models/chat/i-chat-list';

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
  private chatsSubject = new BehaviorSubject<IChatList[]>([]);
  public readonly chats$ = this.chatsSubject.asObservable();

  // 2. Wiadomości aktywnego czatu (dla ChatDetail)
  private activeMessagesSubject = new BehaviorSubject<IMessage[]>([]);
  public readonly activeMessages$ = this.activeMessagesSubject.asObservable();

  // 3. Stan wewnętrzny
  private currentActiveChatId: string | null = null;

  constructor() {
    this.listenToIncomingMessages(); // Dodano tylko nasłuchiwanie
  }

  // =================================================================
  // 1. WEBSOCKET & SYNC LOGIC (The Brain)
  // =================================================================

  private listenToIncomingMessages() {
    this.wsService.notificationSubject.pipe(
      filter((n: any) => n && n.type === 'CHAT_MESSAGE')
    ).subscribe(notif => {
      this.handleRealTimeMessage(notif);
    });
  }

  private handleRealTimeMessage(notif: any) {
    const chatId = notif.data.chatId;

    // A. Aktualizacja Listy Czatów (Sidebar)
    // Przesuwamy czat na górę i aktualizujemy ostatnią wiadomość
    this.updateChatListState(chatId, notif);

    if (!chatId) return;

    // B. Aktualizacja Szczegółów (Detail)
    // Aktualizujemy tylko jeśli użytkownik patrzy na ten konkretny czat
    if (this.currentActiveChatId === chatId) {
      this.upsertActiveMessage(notif);
    }
  }

  private upsertActiveMessage(notif: any) {
    const sourceData = notif.data || notif;
    const msgId = sourceData.id;

    const currentMsgs = this.activeMessagesSubject.value;
    const existingIndex = currentMsgs.findIndex(m => String(m.id) === String(msgId));

    if (existingIndex > -1) {
      const updatedMessages = [...currentMsgs];
      const existingMsg = updatedMessages[existingIndex];

      if (existingMsg.payload?.type === MessageType.PROPOSAL && sourceData.payload) {
        updatedMessages[existingIndex] = {
          ...existingMsg,
          payload: {
            ...existingMsg.payload,
            status: sourceData.payload.status
          }
        };

        this.activeMessagesSubject.next(updatedMessages);
      }

    } else {
      const newMsg = this.mapToIMessage(notif);
      this.activeMessagesSubject.next([...currentMsgs, newMsg]);
    }
  }

  private updateChatListState(chatId: string, notif: any) {
    const currentChats = this.chatsSubject.value;
    const index = currentChats.findIndex(c => c.id === chatId);

    const lastMessageContent = notif.content ||
      (notif.data?.messageType === 'IMAGE' ? 'Przesłano zdjęcie' : 'Nowa wiadomość');

    const isCurrentlyOpen = this.currentActiveChatId === chatId;

    if (index > -1) {
      const chatToUpdate = currentChats[index];
      const updatedChat = {
        ...chatToUpdate,
        lastMessage: lastMessageContent,
        lastMessageDate: new Date(),
        unreadCount: isCurrentlyOpen ? chatToUpdate.unreadCount : (chatToUpdate.unreadCount || 0) + 1
      };

      const otherChats = currentChats.filter(c => c.id !== chatId);
      this.chatsSubject.next([updatedChat, ...otherChats]);
    } else {
      this.getSingleChatSummary(chatId).subscribe({
        next: (newChat) => {
          const freshList = this.chatsSubject.value;

          if (freshList.some(c => c.id === chatId)) return;

          const chatToAdd = {
            ...newChat,
            lastMessage: lastMessageContent,
            lastMessageDate: new Date().toISOString(),
            unreadCount: isCurrentlyOpen ? 0 : (newChat.unreadCount || 1)
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
  /**
   * Wywoływane gdy użytkownik wchodzi do konkretnego czatu.
   * Resetuje stan wiadomości i ładuje nowe.
   */
  enterChat(chatId: string) {
    this.currentActiveChatId = chatId;
    this.activeMessagesSubject.next([]);

    const currentChats = this.chatsSubject.value;
    const updatedChats = currentChats.map(chat => {
      if (chat.id === chatId && chat.unreadCount > 0) {
        return { ...chat, unreadCount: 0 };
      }
      return chat;
    });

    this.chatsSubject.next(updatedChats);

    this.getMessagesApi(chatId).subscribe(page => {
      const sortedMessages = page.content.reverse();
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

  loadUserChats(page: number, size: number): Observable<IPage<IChatList>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<IPage<IChatList>>(this.baseChatUrl, { params }).pipe(
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

  sendProposal(chatId: string | undefined, productId: string | undefined, amount: number): Observable<any> {
    return this.http.post<any>(`${this.baseMessageUrl}/proposal`, { chatId, productId, amount }).pipe(
      map(dto => this.mapToIMessage(dto)),
      tap(savedMsg => {
        if (this.currentActiveChatId === savedMsg.chatId || this.currentActiveChatId === chatId) {
          const current = this.activeMessagesSubject.value;
          this.activeMessagesSubject.next([...current, savedMsg]);
        }
      })
    );
  }

  updateProposalStatus(messageId: string, newStatus: ProposalStatus): Observable<any> {
    const request = {newStatus};

    return this.http.patch<any>(
      `${this.baseMessageUrl}/${messageId}/proposal-status`,
      request
    ).pipe(
      tap(() => {
        const currentMessages = this.activeMessagesSubject.value;

        const updatedMessages = currentMessages.map(msg => {
          if (msg.id === messageId) {
            return {
              ...msg,
              payload: {
                ...msg.payload,
                status: newStatus
              }
            };
          }
          return msg;
        });

        this.activeMessagesSubject.next(updatedMessages);
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

  markMessagesAsRead(chatId: string): Observable<void> {

    return this.http.patch<void>(`${this.baseMessageUrl}/${chatId}/read`, {});
  }

  getChatById(chatId: string): Observable<IChat> {
    return this.http.get<IChat>(`${this.baseChatUrl}/${chatId}`);
  }

  getChatByProductId(productId: string): Observable<IChat> {
    return this.http.get<IChat>(`${this.baseChatUrl}/product/${productId}`);
  }

  getSingleChatSummary(chatId: string): Observable<IChatList> {
    return this.http.get<IChatList>(`${this.baseChatUrl}/${chatId}/summary`);
  }

  private mapToIMessage(dto: any): IMessage {
    const source = dto.data || dto;

    return {
      id: source.id,
      chatId: source.chatId,
      senderId: source.senderId,
      state: source.state || MessageState.SENT,
      createdAt: new Date(source.createdAt || source.createdDate),

      payload: source.payload
    };
  }
}
