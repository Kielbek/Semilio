import {Component, Input} from '@angular/core';
import {DatePipe} from '@angular/common';
import {LucideAngularModule} from 'lucide-angular';
import {EmptyState} from '../../../products/components/empty-state/empty-state';

@Component({
  selector: 'app-user-ratings',
  imports: [
    LucideAngularModule,
    DatePipe,
    EmptyState
  ],
  templateUrl: './user-ratings.html',
  styleUrl: './user-ratings.css'
})
export class UserRatings {
  @Input() ratings: any[] = [];
  @Input() isMine: boolean = false;
}
