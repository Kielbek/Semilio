import { Component, ElementRef, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ArrowLeft, LUCIDE_ICONS, LucideAngularModule, LucideIconProvider } from 'lucide-angular';
import { Subscription } from 'rxjs';
import { tap } from 'rxjs/operators';

import { ChatInput } from '../chat-input/chat-input';
import { ChatMessages } from '../chat-messages/chat-messages';
import {ChatDetailFacade, SendMessageEvent} from '../../../../core/facades/chat-detail.facade';

@Component({
  selector: 'app-chat-detail',
  standalone: true,
  imports: [ChatInput, ChatMessages, LucideAngularModule, CommonModule],
  templateUrl: './chat-detail.html',
  styleUrl: './chat-detail.css',
  providers: [
    ChatDetailFacade,
    { provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ ArrowLeft }) }
  ],
  host: { class: 'flex flex-col w-full h-full overflow-hidden bg-white' }
})
export class ChatDetail implements OnInit, OnDestroy {
  private facade = inject(ChatDetailFacade);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  readonly chatInfo$ = this.facade.chatInfo$;

  readonly messages$ = this.facade.messages$.pipe(
    tap(() => {
      setTimeout(() => this.handleScrollAfterRender(), 50);
    })
  );

  readonly currentUserId = this.facade.currentUserId;
  private routeSub?: Subscription;

  private previousScrollHeight = 0;
  private isRestoringScroll = false;

  ngOnInit() {
    this.routeSub = this.route.paramMap.subscribe(params => {
      this.facade.initChat({
        id: params.get('id'),
        productId: params.get('productId')
      }, history.state);
    });
  }

  onSendMessage(event: SendMessageEvent): void {
    this.facade.sendMessage(event.content, event.file);
  }

  goBackToList(): void {
    this.router.navigate(['../'], { relativeTo: this.route, replaceUrl: true });
  }

  onScroll(event: Event): void {
    const element = event.target as HTMLElement;

    // Jeśli przewinięto na samą górę (scrollTop = 0)
    if (element.scrollTop === 0) {

      // 1. Zapamiętujemy obecną wysokość kontenera
      this.previousScrollHeight = element.scrollHeight;
      this.isRestoringScroll = true;

      // 2. Prosimy fasadę o starsze wiadomości
      this.facade.loadMoreMessages().subscribe();
    }
  }

  private handleScrollAfterRender() {
    // requestAnimationFrame czeka, aż Angular zaktualizuje DOM (wyświetli nowe dymki)
    requestAnimationFrame(() => {
      if (!this.scrollContainer?.nativeElement) return;
      const el = this.scrollContainer.nativeElement;

      if (this.isRestoringScroll) {
        // SCENARIUSZ A: Ładowanie historii
        // Nowa wysokość jest większa, bo doszły wiadomości na górze.
        // Obliczamy różnicę i przesuwamy scroll, żeby użytkownik widział to samo co przed chwilą.
        const newHeight = el.scrollHeight;
        const diff = newHeight - this.previousScrollHeight;

        el.scrollTop = diff;
        this.isRestoringScroll = false;
      } else {
        // SCENARIUSZ B: Nowa wiadomość (zwykła rozmowa) -> przewiń na dół
        // (Opcjonalnie: możesz tu dodać warunek, żeby nie scrollować, jeśli user czyta historię)
        el.scrollTop = el.scrollHeight;
      }
    });
  }

  ngOnDestroy() {
    this.routeSub?.unsubscribe();
  }
}
