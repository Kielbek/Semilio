import {Component, Input} from '@angular/core';
import {
  Heart,
  LUCIDE_ICONS,
  LucideAngularModule,
  LucideIconProvider,
  MessageCircle,
  Search,
  Shirt, Star
} from 'lucide-angular';

@Component({
  selector: 'app-empty-state',
  imports: [
    LucideAngularModule,
  ],
  providers: [
    {
      provide: LUCIDE_ICONS,
      useValue: new LucideIconProvider({
        Search,
        Heart,
        Shirt,
        MessageCircle,
        Star
      })
    }
  ],
  templateUrl: './empty-state.html',
  styleUrl: './empty-state.css'
})
export class EmptyState {
  @Input({ required: true }) title = '';
  @Input() description = '';
  @Input() icon = '';
}
