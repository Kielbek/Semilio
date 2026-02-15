import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserProfileLayout } from './user-profile-layout';

describe('UserProfileLayout', () => {
  let component: UserProfileLayout;
  let fixture: ComponentFixture<UserProfileLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserProfileLayout]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserProfileLayout);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
