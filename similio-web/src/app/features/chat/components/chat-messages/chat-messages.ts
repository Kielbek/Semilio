import {Component, Input} from '@angular/core';
import {DatePipe, NgClass} from '@angular/common';
import {IMessage, MessageType} from '../../../../core/models/i-message';
import {CheckCheck, LUCIDE_ICONS, LucideAngularModule, LucideIconProvider} from 'lucide-angular';

@Component({
  selector: 'app-chat-messages',
  imports: [NgClass, LucideAngularModule, DatePipe],
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
  @Input() message!: IMessage;
  @Input() currentUserId!: string;

  get isCurrentUser(): boolean {
    return this.message.senderId === this.currentUserId;
  }

  get isImageMessage(): boolean {
    return this.message.type === MessageType.IMAGE;
  }

  protected readonly MessageType = MessageType;
}
