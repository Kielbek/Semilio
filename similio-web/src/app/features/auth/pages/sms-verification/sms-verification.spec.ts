import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SmsVerification } from './sms-verification';

describe('SmsVerification', () => {
  let component: SmsVerification;
  let fixture: ComponentFixture<SmsVerification>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SmsVerification]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SmsVerification);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
