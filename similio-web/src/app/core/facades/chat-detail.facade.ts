import {inject, Injectable, OnDestroy} from '@angular/core';
import {Router} from '@angular/router';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {ChatService} from '../service/chat-service';
import {UserService} from '../service/user-service';
import {IChatList} from '../models/chat/i-chat-list';
import {IChat} from '../models/chat/i-chat';

export interface SendMessageEvent {
  content: string;
  file: File | null;
}

@Injectable()
export class ChatDetailFacade implements OnDestroy {
  private chatService = inject(ChatService);
  private userService = inject(UserService);
  private router = inject(Router);

  // === DANE Z SERWISU ===
  // Komponent Detail subskrybuje to pole
  readonly messages$ = this.chatService.activeMessages$;

  // Informacje o nagłówku (tytuł, cena itp.) trzymamy w Facade lub pobieramy z serwisu
  // Tu zostawiam BehaviorSubject dla nagłówka, bo to dane statyczne dla widoku
  private chatInfoSubject = new BehaviorSubject<IChat | null>(null);
  readonly chatInfo$ = this.chatInfoSubject.asObservable();

  // Stan lokalny nawigacji
  private currentChatId: string | null = null;
  private isNewChat = false;
  private draftContext: any = null;

  private currentPage = 0;
  private isLastPage = false;
  private isLoadingHistory = false;

  get currentUserId(): string {
    return this.userService.getLoggedUserId();
  }

  // === INICJALIZACJA ===

  initChat(params: { id?: string | null, productId?: string | null }, stateData?: any) {
    this.draftContext = stateData?.chatContext || null;

    // A. Wejście przez ID czatu (np. z listy)
    if (params.id && params.id !== this.currentChatId) {
      this.enterExistingChat(params.id);
    }
    // B. Wejście przez Produkt (np. przycisk "Napisz do sprzedawcy")
    else if (params.productId) {
      this.handleProductEntry(params.productId);
    }
  }

  // === AKCJE (Delegacja do serwisu) ===

  sendMessage(content: string, file: File | null) {
    if (this.isNewChat) {
      this.createNewChatFlow(content, file);
      return;
    }

    // Scenariusz 2: Istniejąca rozmowa
    if (this.currentChatId) {
      if (file) {
        this.chatService.uploadMedia(this.currentChatId, file).subscribe({
          error: err => console.error('Upload failed', err)
        });
      }

      if (content.trim()) {
        const payload = { content, chatId: this.currentChatId };
        this.chatService.sendMessage(payload).subscribe({
          error: err => console.error('Send failed', err)
        });
      }
    }
  }

  // === LOGIKA POMOCNICZA ===

  private enterExistingChat(chatId: string) {
    this.currentChatId = chatId;
    this.isNewChat = false;

    this.currentPage = 0;
    this.isLastPage = false;
    this.isLoadingHistory = false;

    // 1. Mówimy serwisowi: "Jestem w pokoju X, zacznij nasłuchiwać i ładuj dane"
    this.chatService.enterChat(chatId);

    this.chatService.markMessagesAsRead(chatId).subscribe({
      error: (err: any) => console.error('Nie udało się oznaczyć jako przeczytane', err)
    });

    this.chatService.getChatById(chatId).pipe(
      catchError(() => {
        this.router.navigate(['/chat'], { replaceUrl: true });
        return of(null);
      })
    ).subscribe(info => this.chatInfoSubject.next(info));

  }

  private handleProductEntry(productId: string) {
    this.chatService.getChatByProductId(productId).pipe(
      tap(chat => {
        this.router.navigate(['/chat', chat.id], { replaceUrl: true, state: { chatInfo: chat } });
        this.enterExistingChat(chat.id);
      }),
      catchError(() => {
        this.setupNewChatState();
        return of(null);
      })
    ).subscribe();
  }

  loadMoreMessages(): Observable<boolean> {
    if (this.isLoadingHistory || this.isLastPage || !this.currentChatId) {
      return of(false);
    }

    this.isLoadingHistory = true;
    const nextPage = this.currentPage + 1;

    return this.chatService.loadOlderMessages(this.currentChatId, nextPage).pipe(
      map(response => {
        this.isLoadingHistory = false;

        if (response.content && response.content.length > 0) {
          const currentPageNumber = response.page.number;
          const totalPages = response.page.totalPages;

          this.isLastPage = currentPageNumber >= (totalPages - 1);

          this.currentPage = currentPageNumber;

          return true;
        } else {
          this.isLastPage = true;
          return false;
        }
      }),
      catchError((err) => {
        console.error('Błąd ładowania historii czatu:', err);
        this.isLoadingHistory = false;
        return of(false);
      })
    );
  }

  private createNewChatFlow(content: string, file: File | null) {
    const payload = {
      content: content || 'Rozpoczęto rozmowę',
      productId: this.draftContext?.productId
    };

    this.chatService.sendMessage(payload).subscribe(response => {
      if (response.chatId) {
        const newChatId = response.chatId;

        this.currentChatId = newChatId;
        this.isNewChat = false;

        // Zmieniamy URL
        this.router.navigate(['/chat', newChatId], { replaceUrl: true });

        // Aktywujemy serwis dla nowego ID
        this.chatService.enterChat(newChatId);

        // Jeśli było też zdjęcie, wysyłamy je teraz
        if (file) {
          this.chatService.uploadMedia(newChatId, file).subscribe();
        }
      }
    });
  }

  private setupNewChatState() {
    this.isNewChat = true;
    this.currentChatId = null;
    this.chatService.leaveChat();

    if (this.draftContext) {
      const draftChatInfo: IChat = {
        id: '',
        productId: this.draftContext.productId,
        productTitle: this.draftContext.productTitle,
        productImage: this.draftContext.productImage,
        productPrice: this.draftContext.productPrice,
      };
      this.chatInfoSubject.next(draftChatInfo);
    } else {
      this.chatInfoSubject.next(null);
    }
  }

  ngOnDestroy() {
    this.chatService.leaveChat();
  }
}
