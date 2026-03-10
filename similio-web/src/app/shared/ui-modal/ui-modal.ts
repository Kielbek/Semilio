import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnInit,
  OnDestroy,
  HostListener,
  inject
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Spinner } from '../spinner/spinner';

@Component({
  selector: 'app-ui-modal',
  imports: [Spinner],
  templateUrl: './ui-modal.html',
  styleUrl: './ui-modal.css',
})
export class UiModal implements OnInit, OnDestroy {
  private readonly document = inject(DOCUMENT);

  @Input() title: string = '';
  @Input() showBackButton: boolean = false;
  @Input() isLoading = false;

  @Output() close = new EventEmitter<void>();
  @Output() back = new EventEmitter<void>();

  private readonly _stateKey = `modal_${Math.random().toString(36).substring(2, 9)}`;
  private scrollPosition = 0;

  ngOnInit(): void {
    window.history.pushState({ [this._stateKey]: true }, '');

    this.lockScroll();
  }

  ngOnDestroy(): void {
    setTimeout(() => {
      this.unlockScroll();
    }, 300);


    if (typeof window !== 'undefined' && window.history.state?.[this._stateKey]) {
      window.history.back();
    }
  }

  @HostListener('window:popstate')
  onPopState(): void {
    this.close.emit();
  }

  triggerClose(): void {
    if (this.document.activeElement instanceof HTMLElement) {
      this.document.activeElement.blur();
    }

    if (typeof window !== 'undefined' && window.history.state?.[this._stateKey]) {
      window.history.back();
    } else {
      this.close.emit();
    }
  }

  private lockScroll(): void {

    this.scrollPosition = Math.round(window.scrollY);

    if ('scrollRestoration' in window.history) {
      window.history.scrollRestoration = 'manual';
    }

    this.document.body.style.position = 'fixed';
    this.document.body.style.top = `-${this.scrollPosition}px`;
    this.document.body.style.width = '100%';
    this.document.body.style.overflow = 'hidden';
  }

  private unlockScroll(): void {

    const html = this.document.documentElement;
    const body = this.document.body;
    const pos = this.scrollPosition;

    html.style.setProperty('scroll-behavior', 'auto', 'important');

    body.style.removeProperty('position');
    body.style.removeProperty('top');
    body.style.removeProperty('width');
    body.style.removeProperty('overflow');

    window.scrollTo(0, pos);

    requestAnimationFrame(() => {
      html.style.removeProperty('scroll-behavior');
      if ('scrollRestoration' in window.history) {
        window.history.scrollRestoration = 'auto';
      }
    });
  }
}
