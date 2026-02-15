import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrivacySettings } from './privacy-settings';

describe('PrivacySettings', () => {
  let component: PrivacySettings;
  let fixture: ComponentFixture<PrivacySettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrivacySettings]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrivacySettings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
