import {Component, inject, Input} from '@angular/core';
import {DatePipe, NgClass} from '@angular/common';
import {IMessage, MessageType, ProposalStatus} from '../../../../core/models/chat/i-message';
import {CheckCheck, LUCIDE_ICONS, LucideAngularModule, LucideIconProvider} from 'lucide-angular';
import {Button} from '../../../../shared/button/button';
import {ChatService} from '../../../../core/service/chat-service';

@Component({
  selector: 'app-chat-messages',
  imports: [NgClass, LucideAngularModule, DatePipe, Button],
  templateUrl: './chat-messages.html',
  styleUrl: './chat-messages.css',
  providers: [
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({ CheckCheck })
    }
  ]
})
export class ChatMessages {
  private chatService = inject(ChatService);

  @Input() message!: IMessage;
  @Input() currentUserId!: string;

  get isCurrentUser(): boolean {
    return this.message.senderId === this.currentUserId;
  }

  handleProposalResponse(status: ProposalStatus) {
    this.chatService.updateProposalStatus(this.message.id, status).subscribe();
  }

  protected readonly MessageType = MessageType;
  protected readonly ProposalStatus = ProposalStatus;
}
