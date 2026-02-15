import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Selling } from './selling';

describe('Selling', () => {
  let component: Selling;
  let fixture: ComponentFixture<Selling>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Selling]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Selling);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
