import { TestBed } from '@angular/core/testing';

import { ViewTrackerService } from './view-tracker-service';

describe('ViewTrackerService', () => {
  let service: ViewTrackerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ViewTrackerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
