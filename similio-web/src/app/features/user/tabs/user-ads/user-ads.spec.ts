import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserAds } from './user-ads';

describe('UserAds', () => {
  let component: UserAds;
  let fixture: ComponentFixture<UserAds>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserAds]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserAds);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
