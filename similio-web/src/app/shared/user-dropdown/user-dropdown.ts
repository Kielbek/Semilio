import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input, OnInit,
  Output,
  signal
} from '@angular/core';
import {RouterLink} from '@angular/router';
import {NgClass} from '@angular/common';
import {ChevronDown, LogOut, LUCIDE_ICONS, LucideAngularModule, LucideIconProvider, User} from 'lucide-angular';
import {IUser} from '../../core/models/i-user';
import {UserAvatar} from '../user-avatar/user-avatar';
import {UserService} from '../../core/service/user-service';
import {AppConfig} from '../../core/config/app-paths';

@Component({
  selector: 'app-user-dropdown',
  standalone: true,
  imports: [RouterLink, LucideAngularModule, NgClass, UserAvatar],
  templateUrl: './user-dropdown.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({ User, ChevronDown, LogOut })
    }
  ]
})
export class UserDropdown implements OnInit {
  private userService = inject(UserService);
  private elementRef = inject(ElementRef);

  @Input({ required: true }) user!: IUser;
  @Output() logout = new EventEmitter<void>();

  readonly isOpen = signal(false);
  readonly paths = AppConfig;
  currentId?: string;

  ngOnInit() {
    this.userService.getLoggedUserId$().subscribe(id => {
      this.currentId = id || 'me';
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (this.isOpen() && !this.elementRef.nativeElement.contains(target)) {
      this.close();
    }
  }

  @HostListener('window:scroll')
  onWindowScroll(): void {
   this.close();
  }

  @HostListener('window:keydown.escape')
  onEscape(): void {
    this.close();
  }

  toggle(): void {
    this.isOpen.update(v => !v);
  }

  close(): void {
    this.isOpen.set(false);
  }

  onLogout(): void {
    this.logout.emit();
    this.close();
  }
}
