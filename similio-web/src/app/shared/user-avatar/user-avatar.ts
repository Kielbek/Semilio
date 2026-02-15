import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';

@Component({
  selector: 'app-user-avatar',
  standalone: true,
  imports: [],
  templateUrl: './user-avatar.html',
  styleUrl: './user-avatar.css'
})
export class UserAvatar implements OnChanges {
  @Input() imageUrl: string | null | undefined = null;
  @Input() name: string | null | undefined = '';
  @Input() sizeClass: string = 'w-16 h-16 md:w-20 md:h-20';

  public showImage: boolean = false;
  public currentImageUrl: string = '';
  public calculatedInitials: string = '';

  private hasLoadingError: boolean = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['imageUrl']) {
      this.hasLoadingError = false;
    }
    this.updateState();
  }

  onImageError(): void {
    this.hasLoadingError = true;
    this.updateState();
  }

  private updateState(): void {
    this.calculatedInitials = this.name ? this.name.charAt(0) : 'S';

    if (this.imageUrl && !this.hasLoadingError) {
      this.currentImageUrl = this.imageUrl;
      this.showImage = true;
    } else {
      this.showImage = false;
    }
  }
}
