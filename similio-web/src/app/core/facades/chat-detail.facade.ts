import {inject, Injectable, OnDestroy} from '@angular/core';
import {Router} from '@angular/router';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {ChatService} from '../service/chat-service';
import {UserService} from '../service/user-service';
import {IChat} from '../models/i-chat';

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
      this.enterExistingChat(params.id, stateData?.chatInfo);
    }
    // B. Wejście przez Produkt (np. przycisk "Napisz do sprzedawcy")
    else if (params.productId) {
      this.handleProductEntry(params.productId);
    }
  }

  // === AKCJE (Delegacja do serwisu) ===

  sendMessage(content: string, file: File | null) {
    // Scenariusz 1: Nowa rozmowa (brak ID czatu)
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

  private enterExistingChat(chatId: string, cachedInfo?: IChat) {
    this.currentChatId = chatId;
    this.isNewChat = false;

    this.currentPage = 0;
    this.isLastPage = false;
    this.isLoadingHistory = false;

    // 1. Mówimy serwisowi: "Jestem w pokoju X, zacznij nasłuchiwać i ładuj dane"
    this.chatService.enterChat(chatId);

    // 2. Ładujemy nagłówek (Info o produkcie/użytkowniku)
    if (cachedInfo) {
      this.chatInfoSubject.next(cachedInfo);
    } else {
      this.chatService.getChatById(chatId).pipe(
        catchError(() => {
          this.router.navigate(['/chat'], { replaceUrl: true });
          return of(null);
        })
      ).subscribe(info => this.chatInfoSubject.next(info));
    }
  }

  private handleProductEntry(productId: string) {
    this.chatService.getChatByProductId(productId).pipe(
      tap(chat => {
        // Czat istnieje -> wchodzimy w niego
        this.router.navigate(['/chat', chat.id], { replaceUrl: true });
        this.enterExistingChat(chat.id);
      }),
      catchError(() => {
        // Czat nie istnieje -> tryb tworzenia (Draft)
        this.setupNewChatState();
        return of(null);
      })
    ).subscribe();
  }


  // NOWA METODA: Wywoływana przez scroll w komponencie
  loadMoreMessages(): Observable<boolean> {
    // Jeśli już ładujemy, lub to ostatnia strona, lub nie ma ID -> przerwij
    if (this.isLoadingHistory || this.isLastPage || !this.currentChatId) {
      return of(false);
    }

    this.isLoadingHistory = true;
    this.currentPage++; // Idziemy stronę wstecz w historii

    return this.chatService.loadOlderMessages(this.currentChatId, this.currentPage).pipe(
      map(page => {
        this.isLoadingHistory = false;

        // Jeśli pobraliśmy mniej niż rozmiar strony (np. 20), to znaczy że to koniec
        if (page.last || page.content.length === 0) {
          this.isLastPage = true;
        }
        return true; // Udało się załadować
      }),
      catchError(() => {
        this.isLoadingHistory = false;
        this.currentPage--; // Cofamy licznik w razie błędu
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

    this.chatInfoSubject.next({
      id: 'new',
      productTitle: this.draftContext?.productTitle || 'Nowa rozmowa',
      productPrice: this.draftContext?.productPrice || 0,
      productImage: {
        url: this.draftContext?.productImage || ''
      },
    } as any);
  }

  ngOnDestroy() {
    this.chatService.leaveChat();
  }
}
