import { TestBed } from '@angular/core/testing';

import { INotification } from './notification';

describe('INotification', () => {
  let service: Notification;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Notification);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
