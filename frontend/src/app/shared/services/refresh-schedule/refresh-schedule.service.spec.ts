import { TestBed } from '@angular/core/testing';
import { RefreshScheduleService } from './refresh-schedule.service';

describe('RefreshScheduleService', () => {
  let service: RefreshScheduleService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RefreshScheduleService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
