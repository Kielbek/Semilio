import { inject, Injectable } from '@angular/core';
import { ToastService } from './toast-service';

export interface FileSelectionResult {
  file: File;
  preview: string;
}

@Injectable({
  providedIn: 'root',
})
export class FileService {
  private toastService = inject(ToastService);

  private readonly MAX_INPUT_SIZE = 15 * 1024 * 1024;
  private readonly COMPRESSION_QUALITY = 0.7;
  private readonly MAX_WIDTH = 1920;

  async processSingleImage(event: Event): Promise<FileSelectionResult | null> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) return null;

    const results = await this.validateAndProcess([file]);
    if (results.length === 0) {
      this.resetInput(input);
      return null;
    }
    return results[0];
  }

  async processMultipleImages(event: Event): Promise<FileSelectionResult[]> {
    const input = event.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];

    if (files.length === 0) return [];

    return await this.validateAndProcess(files);
  }

  private async validateAndProcess(files: File[]): Promise<FileSelectionResult[]> {
    const validResults: FileSelectionResult[] = [];

    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        this.toastService.show(`Plik ${file.name} nie jest obrazem`, 'error');
        continue;
      }

      if (file.size > this.MAX_INPUT_SIZE) {
        this.toastService.show(`Plik ${file.name} jest za duży (max 15MB)`, 'error');
        continue;
      }

      try {
        const compressedBlob = await this.compressImage(file);

        const compressedFile = new File([compressedBlob], file.name.replace(/\.[^/.]+$/, "") + ".jpg", {
          type: 'image/jpeg',
          lastModified: Date.now(),
        });

        const preview = await this.generatePreview(compressedFile);

        validResults.push({ file: compressedFile, preview });
      } catch (error) {
        this.toastService.show(`Błąd podczas przetwarzania ${file.name}`, 'error');
        console.error(error);
      }
    }
    return validResults;
  }

  private compressImage(file: File): Promise<Blob> {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.src = URL.createObjectURL(file);

      img.onload = () => {
        URL.revokeObjectURL(img.src);
        const canvas = document.createElement('canvas');
        let width = img.width;
        let height = img.height;

        if (width > this.MAX_WIDTH) {
          height = (height * this.MAX_WIDTH) / width;
          width = this.MAX_WIDTH;
        }

        canvas.width = width;
        canvas.height = height;

        const ctx = canvas.getContext('2d');
        if (!ctx) return reject('Nie udało się uzyskać kontekstu Canvas');

        ctx.drawImage(img, 0, 0, width, height);

        canvas.toBlob(
          (blob) => {
            if (blob) resolve(blob);
            else reject('Błąd generowania Bloba');
          },
          'image/jpeg',
          this.COMPRESSION_QUALITY
        );
      };

      img.onerror = () => reject('Błąd ładowania obrazu do kompresji');
    });
  }

  generatePreview(file: File): Promise<string> {
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (e) => resolve(e.target?.result as string);
      reader.readAsDataURL(file);
    });
  }

  resetInput(input: HTMLInputElement | undefined): void {
    if (input) input.value = '';
  }
}
