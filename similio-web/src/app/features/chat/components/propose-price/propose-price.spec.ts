import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProposePrice } from './propose-price';

describe('ProposePrice', () => {
  let component: ProposePrice;
  let fixture: ComponentFixture<ProposePrice>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProposePrice]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProposePrice);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
