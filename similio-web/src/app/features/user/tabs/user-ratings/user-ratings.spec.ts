import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserRatings } from './user-ratings';

describe('UserRatings', () => {
  let component: UserRatings;
  let fixture: ComponentFixture<UserRatings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserRatings]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserRatings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
