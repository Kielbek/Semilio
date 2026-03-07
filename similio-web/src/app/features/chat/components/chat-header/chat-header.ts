import {Component, EventEmitter, Input, Output} from '@angular/core';
import {IChat} from '../../../../core/models/chat/i-chat';
import {DecimalPipe} from '@angular/common';
import {Button} from '../../../../shared/button/button';
import {ArrowLeft, LUCIDE_ICONS, LucideAngularModule, LucideIconProvider} from 'lucide-angular';
import {ChatDetailFacade} from '../../../../core/facades/chat-detail.facade';

@Component({
  selector: 'app-chat-header',
  imports: [
    Button,
    DecimalPipe,
    LucideAngularModule
  ],
  templateUrl: './chat-header.html',
  styleUrl: './chat-header.css',
  providers: [
    ChatDetailFacade,
    { provide: LUCIDE_ICONS, useValue: new LucideIconProvider({ ArrowLeft }) }
  ],
})
export class ChatHeader {
  @Input() chat: IChat | null = null;
  @Output() back = new EventEmitter<void>();
  @Output() openProposal = new EventEmitter<void>();

}
