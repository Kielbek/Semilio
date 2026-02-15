import {Component, EventEmitter, inject, Input, Output, signal} from '@angular/core';
import {FileService} from '../../../../core/service/file-service';
import {list} from 'postcss';
import {IImage} from '../../../../core/models/product/i-image';

export interface GalleryItem {
  preview: string;
  file: File | null;
  originalData?: IImage;
}

@Component({
  selector: 'app-image-uploader',
  standalone: true,
  imports: [],
  templateUrl: './image-uploader.html',
  styleUrl: './image-uploader.css'
})
export class ImageUploader {
  private fileService = inject(FileService);

  @Input() maxImages = 9;
  @Input()
  set existingImages(images: IImage[]) {
    if (images && images.length > 0) {
      const backendItems: GalleryItem[] = images.map(img => ({
        preview: img.url,
        file: null,
        originalData: img
      }));
      this.items.set(backendItems);
    }
  }

  @Output() filesChanged = new EventEmitter<File[]>();
  @Output() existingImagesChanged = new EventEmitter<string[]>();

  items = signal<GalleryItem[]>([]);
  isProcessing = signal(false);
  isDragging = signal(false);

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(true);
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(false);
  }

  async onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(false);
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      const mockEvent = { target: { files } } as unknown as Event;
      await this.onFileSelected(mockEvent);
    }
  }

  async onFileSelected(event: Event) {
    if (this.items().length >= this.maxImages) return;

    this.isProcessing.set(true);

    try {
      const results = await this.fileService.processMultipleImages(event);

      if (results.length > 0) {
        const newItems: GalleryItem[] = results.map(r => ({
          preview: r.preview,
          file: r.file,
          originalData: undefined
        }));

        const updatedItems = [...this.items(), ...newItems].slice(0, this.maxImages);
        this.items.set(updatedItems);

        this.emitChanges();
      }
    } finally {
      this.isProcessing.set(false);
      const input = event.target as HTMLInputElement;
      if (input) input.value = '';
    }
  }

  removePhoto(index: number) {
    const currentItems = this.items();
    const updatedItems = currentItems.filter((_, i) => i !== index);
    this.items.set(updatedItems);

    this.emitChanges();
  }

  private emitChanges() {
    const currentItems = this.items();

    const newFiles = currentItems
      .filter(item => item.file !== null)
      .map(item => item.file as File);

    const remainingExistingUrls = currentItems
      .filter(item => item.file === null)
      .map(item => item.originalData?.url || item.preview);

    this.filesChanged.emit(newFiles);
    this.existingImagesChanged.emit(remainingExistingUrls);
  }

  protected readonly list = list;
}
