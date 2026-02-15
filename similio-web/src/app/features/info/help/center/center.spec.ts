import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Center } from './center';

describe('Center', () => {
  let component: Center;
  let fixture: ComponentFixture<Center>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Center]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Center);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
