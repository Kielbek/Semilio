import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserProfileHeader } from './user-profile-header';

describe('UserProfileHeader', () => {
  let component: UserProfileHeader;
  let fixture: ComponentFixture<UserProfileHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserProfileHeader]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserProfileHeader);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
