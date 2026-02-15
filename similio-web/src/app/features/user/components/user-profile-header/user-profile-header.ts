import {Component, Input} from '@angular/core';
import {LUCIDE_ICONS, LucideAngularModule, LucideIconProvider, Settings} from 'lucide-angular';
import {DatePipe} from '@angular/common';
import {IUserPublic} from '../../../../core/models/i-user-public';
import {RouterLink} from '@angular/router';
import {UserAvatar} from '../../../../shared/user-avatar/user-avatar';

@Component({
  selector: 'app-user-profile-header',
  imports: [
    LucideAngularModule,
    DatePipe,
    RouterLink,
    UserAvatar
  ],
  templateUrl: './user-profile-header.html',
  styleUrl: './user-profile-header.css',
  providers: [
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({
        Settings
      })
    }
  ],
})
export class UserProfileHeader {
  @Input({ required: true }) user?: IUserPublic | null;
  @Input({ required: true }) isMyProfile?: boolean;

  get displayName(): string {
    return `${this.user?.nickName}`
  }

  get initials(): string {
    return this.user?.nickName?.charAt(0).toUpperCase() ?? '';
  }
}
