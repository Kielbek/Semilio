import {
  Component,
  HostListener,
  Input,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import {IImage} from '../../../../core/models/product/i-image';

@Component({
  selector: 'app-image-gallery',
  imports: [],
  templateUrl: './image-gallery.html',
  styleUrl: './image-gallery.css',
})
export class ImageGallery implements OnChanges {
  @Input() images: IImage[] = [];
  @Input() startIndex: number = 0;
  selectedIndex: number = 0;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['startIndex']) {
      this.selectedIndex = this.startIndex;
    }
  }

  get selectedImage(): IImage | null {
    return this.selectedIndex !== null ? this.images[this.selectedIndex] : null;
  }

  prevImage() {
    if (this.selectedIndex !== null) {
      this.selectedIndex =
        (this.selectedIndex - 1 + this.images.length) % this.images.length;
    }
  }

  nextImage() {
    if (this.selectedIndex !== null) {
      this.selectedIndex = (this.selectedIndex + 1) % this.images.length;
    }
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboard(event: KeyboardEvent) {
    if (this.selectedIndex !== null) {
      if (event.key === 'ArrowLeft') {
        this.prevImage();
      } else if (event.key === 'ArrowRight') {
        this.nextImage();
      }
    }
  }
}
