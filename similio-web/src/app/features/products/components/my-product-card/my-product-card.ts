import {Component, ElementRef, EventEmitter, HostListener, inject, Input, Output, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {IProductCard} from '../../../../core/models/product/i-product-card';
import {ProductService} from '../../../../core/service/product.service';
import {Spinner} from '../../../../shared/spinner/spinner';
import {finalize} from 'rxjs';
import {DecimalPipe} from '@angular/common';
import {Status} from '../../../../core/models/product/status';
import {AppConfig} from '../../../../core/config/app-paths';

type ProductAction = 'edit' | 'hide' | 'delete';

@Component({
  selector: 'app-my-product-card',
  standalone: true,
  imports: [RouterLink, Spinner, DecimalPipe],
  templateUrl: './my-product-card.html',
  styleUrl: './my-product-card.css'
})
export class MyProductCard {
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef);
  protected readonly path = AppConfig;
  protected readonly Status = Status;

  @Input({ required: true }) product!: IProductCard;

  @Output() deleted = new EventEmitter<string>();

  readonly isImageLoaded = signal(false);
  readonly isMenuOpen = signal(false);
  readonly isProcessing = signal(false);

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isMenuOpen.set(false);
    }
  }

  toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.isMenuOpen.update(prev => !prev);
  }

  handleAction(type: ProductAction): void {
    this.isMenuOpen.set(false);

    switch (type) {
      case 'edit':
        this.router.navigate([this.path.LINKS.PRODUCT.EDIT_ROOT, this.product.id]);
        break;
      case 'hide':
        this.toggleVisibility();
        break;
      case 'delete':
        this.confirmDeletion();
        break;
    }
  }

  private toggleVisibility(): void {
    this.isProcessing.set(true);

    this.productService.toggleVisibility(this.product.id)
      .pipe(finalize(() => this.isProcessing.set(false)))
      .subscribe({
        next: () => {
          this.product.status = this.product.status === Status.ACTIVE ? Status.HIDDEN : Status.ACTIVE;

        },
        error: () => console.error('Nie udało się zmienić widoczności')
      });
  }

  private confirmDeletion(): void {
    const confirmed = confirm(`Czy na pewno chcesz usunąć: ${this.product.title}?`);

    if (confirmed) {
      this.isProcessing.set(true);

      this.productService.deleteProduct(this.product.id)
        .pipe(finalize(() => this.isProcessing.set(false)))
        .subscribe({
          next: () => {
            this.deleted.emit(this.product.id);
          },
          error: () => console.error('Błąd podczas usuwania ogłoszenia')
        });
    }
  }
}
