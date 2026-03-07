import {Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {DatePipe, NgClass} from '@angular/common';
import {Router, RouterLinkActive} from '@angular/router';
import {IChatList} from '../../../../core/models/chat/i-chat-list';
import {UserAvatar} from '../../../../shared/user-avatar/user-avatar';

@Component({
  selector: 'app-chat-list',
  templateUrl: './chat-list.html',
  styleUrl: './chat-list.css',
  imports: [
    DatePipe,
    RouterLinkActive,
    UserAvatar,
    NgClass
  ]
})
export class ChatList {
  private router = inject(Router);

  @Input() conversations: IChatList[] = [];
  @Input() loading = false;
  @Input() hasMore = false;
  @Output() onLoadMore = new EventEmitter<void>();

  openChat(chat: any) {
    const isSwitchingChats = this.router.url.includes('/chat/') && this.router.url !== '/chat';

    this.router.navigate(['/chat', chat.id], {
      replaceUrl: isSwitchingChats,
      state: { chatInfo: chat }
    });
  }
}
