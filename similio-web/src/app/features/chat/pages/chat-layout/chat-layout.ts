import {Component, HostListener, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {LayoutService} from '../../../../core/service/layout-service';
import {NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {BehaviorSubject, combineLatest, filter, map, startWith, Subscription} from 'rxjs';
import {ChatService} from '../../../../core/service/chat-service';
import {ChatList} from '../../components/chat-list/chat-list';
import {AsyncPipe} from '@angular/common';
import {WebSocketService} from '../../../../core/service/web-socket-service';

@Component({
  selector: 'app-chat-layout',
  imports: [
    ChatList,
    RouterOutlet,
    AsyncPipe
  ],
  templateUrl: './chat-layout.html',
  styleUrl: './chat-layout.css'
})
export class ChatLayout implements OnInit , OnDestroy {
  private layoutService = inject(LayoutService);
  private chatService = inject(ChatService); // <-- Tylko ten serwis danych
  private router = inject(Router);

  readonly chats$ = this.chatService.chats$;

  // Stan widoku (Paginacja i UI)
  currentPage = 0;
  isLastPage = false;
  loading = signal(false);

  // Stan UI (RWD)
  readonly headerHeight$ = this.layoutService.headerHeight$;
  private screenWidth$ = new BehaviorSubject<number>(window.innerWidth);

  ngOnInit() {
    // Inicjalne ładowanie listy czatów
    this.loadMoreChats();
    // Nie łączymy się tu z WebSocketem! ChatService to robi sam.
  }

  loadMoreChats() {
    if (this.loading() || this.isLastPage) return;

    this.loading.set(true);

    this.chatService.loadUserChats(this.currentPage, 10).subscribe({
      next: (page) => {
        // Serwis sam aktualizuje 'chats$', my tylko sprawdzamy czy to koniec stron
        // Zakładam że IPage ma pole 'last' lub sprawdzasz długość contentu
        const content = page.content || [];
        this.isLastPage = content.length < 10 || (page as any).last;

        this.currentPage++;
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Błąd ładowania czatów:', err);
        this.loading.set(false);
      }
    });
  }

  // === LOGIKA UI / RWD (Bez zmian) ===

  @HostListener('window:resize', ['$event'])
  onResize(event: UIEvent): void {
    const width = (event.target as Window).innerWidth;
    this.screenWidth$.next(width);
  }

  readonly isChildRouteActive$ = this.router.events.pipe(
    filter(e => e instanceof NavigationEnd),
    map(() => this.hasChildRoute()),
    startWith(this.hasChildRoute())
  );

  readonly isFullMobileView$ = combineLatest([
    this.screenWidth$,
    this.isChildRouteActive$
  ]).pipe(
    map(([width, isActive]) => width < 768 && isActive)
  );

  private hasChildRoute(): boolean {
    const tree = this.router.parseUrl(this.router.url);
    return Object.keys(tree.root.children).length > 0 && this.router.url !== '/chat';
  }

  ngOnDestroy() {
    this.chatService.clearChatsList();
  }
}
