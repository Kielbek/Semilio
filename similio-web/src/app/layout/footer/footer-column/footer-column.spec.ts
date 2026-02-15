import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FooterColumn } from './footer-column';

describe('FooterColumn', () => {
  let component: FooterColumn;
  let fixture: ComponentFixture<FooterColumn>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FooterColumn],
    }).compileComponents();

    fixture = TestBed.createComponent(FooterColumn);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
