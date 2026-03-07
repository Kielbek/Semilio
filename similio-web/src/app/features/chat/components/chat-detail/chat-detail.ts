import {Component, ElementRef, inject, OnInit, signal, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {LucideAngularModule} from 'lucide-angular';
import {tap} from 'rxjs/operators';

import {ChatInput} from '../chat-input/chat-input';
import {ChatMessages} from '../chat-messages/chat-messages';
import {ChatDetailFacade, SendMessageEvent} from '../../../../core/facades/chat-detail.facade';
import {ProposePrice} from '../propose-price/propose-price';
import {ChatHeader} from '../chat-header/chat-header';

@Component({
  selector: 'app-chat-detail',
  standalone: true,
  imports: [ChatInput, ChatMessages, LucideAngularModule, CommonModule, ProposePrice, ChatHeader],
  templateUrl: './chat-detail.html',
  styleUrl: './chat-detail.css',
  providers: [ChatDetailFacade],
  host: { class: 'flex flex-col w-full h-full overflow-hidden bg-white' }
})
export class ChatDetail implements OnInit {
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

  showPriceModal = signal(false);
  private isRestoringScroll = signal(false);

  private previousScrollHeight = 0;

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
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

  openPriceProposal() {
    this.showPriceModal.set(true);
  }

  onScroll(event: Event): void {
    const element = event.target as HTMLElement;

    if (element.scrollTop === 0) {

      this.previousScrollHeight = element.scrollHeight;
      this.isRestoringScroll.set(true);

      this.facade.loadMoreMessages().subscribe();
    }
  }

  private handleScrollAfterRender() {
    requestAnimationFrame(() => {
      if (!this.scrollContainer?.nativeElement) return;
      const el = this.scrollContainer.nativeElement;

      if (this.isRestoringScroll()) {
        const newHeight = el.scrollHeight;
        const diff = newHeight - this.previousScrollHeight;

        el.scrollTop = diff;
        this.isRestoringScroll.set(false);
      } else {
        el.scrollTop = el.scrollHeight;
      }
    });
  }

}
