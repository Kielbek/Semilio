import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyProductCard } from './my-product-card';

describe('MyProductCard', () => {
  let component: MyProductCard;
  let fixture: ComponentFixture<MyProductCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyProductCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyProductCard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
