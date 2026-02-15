import {Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import {EmptyState} from '../../../products/components/empty-state/empty-state';
import {Button} from '../../../../shared/button/button';
import {IChat} from '../../../../core/models/i-chat';

@Component({
  selector: 'app-chat-list',
  templateUrl: './chat-list.html',
  styleUrl: './chat-list.css',
  imports: [
    RouterLink,
    DatePipe,
    RouterLinkActive,
    EmptyState,
    Button
  ]
})
export class ChatList {
  private router = inject(Router);

  @Input() conversations: IChat[] = [];
  @Input() loading = false;
  @Input() hasMore = false;
  @Output() onLoadMore = new EventEmitter<void>();

  openChat(chatId: string) {
    const isSwitchingChats = this.router.url.includes('/chat/') && this.router.url !== '/chat';

    this.router.navigate(['/chat', chatId], {
      replaceUrl: isSwitchingChats
    });
  }
}
