import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UiModal } from './ui-modal';

describe('UiModal', () => {
  let component: UiModal;
  let fixture: ComponentFixture<UiModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UiModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UiModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
