import {Component, ElementRef, EventEmitter, inject, Output, ViewChild} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Button} from '../../../../shared/button/button';
import {SendMessageEvent} from '../../../../core/facades/chat-detail.facade';
import {FileService} from '../../../../core/service/file-service';

@Component({
  selector: 'app-chat-input',
  imports: [FormsModule, Button],
  templateUrl: './chat-input.html',
  styleUrl: './chat-input.css',
})
export class ChatInput {
  private fileService = inject(FileService);

  @Output() send = new EventEmitter<SendMessageEvent>();

  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  selectedImage: File | null = null;
  selectedImagePreview: string | null = null;
  newMessage = '';

  sendMessage(): void {
    const text = this.newMessage.trim();

    if (!text && !this.selectedImage) return;

    this.send.emit({
      content: text,
      file: this.selectedImage
    });

    this.newMessage = '';
    this.removeImage();
  }

  triggerCamera() {
    this.fileInput.nativeElement.click();
  }

  async onFileSelected(event: Event) {
    const result = await this.fileService.processSingleImage(event);
    if (result) {
      this.selectedImage = result.file;
      this.selectedImagePreview = result.preview;
    }
  }

  removeImage() {
    this.selectedImage = null;
    this.selectedImagePreview = null;
    this.fileService.resetInput(this.fileInput?.nativeElement);
  }

}
