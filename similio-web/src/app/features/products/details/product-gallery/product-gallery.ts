import { Component, Input, OnInit } from '@angular/core';
import {NgClass, NgTemplateOutlet} from '@angular/common';
import { ImageGallery } from '../image-gallery/image-gallery';
import { Popup } from '../../../../shared/popup/popup';
import {IImage} from '../../../../core/models/product/i-image';

@Component({
  selector: 'app-product-gallery',
  imports: [NgClass, NgTemplateOutlet, ImageGallery, Popup],
  templateUrl: './product-gallery.html',
  styleUrl: './product-gallery.css',
})
export class ProductGallery {
  @Input({ required: true }) images: IImage[] = [];

  isPopupOpen = false;
  selectedIndex = 0;
  currentIndex = 0;

  getRightContainerClass(): string {
    const len = this.images.length;

    if (len === 2) return 'grid-cols-1 grid-rows-1';

    if (len === 3) return 'grid-cols-1 grid-rows-[1fr_1fr]';

    return 'grid-cols-2 grid-rows-[1fr_1fr]';
  }

  getItemClass(indexInRightCol: number): string {
    const totalImages = this.images.length;

    if (totalImages === 4 && indexInRightCol === 0) {
      return 'col-span-2';
    }

    return '';
  }

  openPopup(index: number) {
    this.selectedIndex = index;
    this.isPopupOpen = true;
  }

  nextImage() {
    this.currentIndex = (this.currentIndex + 1) % this.images.length;
  }

  prevImage() {
    this.currentIndex = (this.currentIndex - 1 + this.images.length) % this.images.length;
  }

}
