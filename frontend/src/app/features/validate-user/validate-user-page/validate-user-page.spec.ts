import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { ValidateUserPage } from './validate-user-page';

describe('ValidateUserPage', () => {
  let fixture: ComponentFixture<ValidateUserPage>;
  let httpTesting: HttpTestingController;

  async function setup(token: string | null) {
    await TestBed.configureTestingModule({
      imports: [ValidateUserPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(token ? { token } : {}) } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ValidateUserPage);
    httpTesting = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  }

  afterEach(() => httpTesting.verify());

  it('should show invalid link when there is no token', async () => {
    await setup(null);

    httpTesting.expectNone(`${environment.apiUrl}/auth/validate_user`);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });

  it('should send the token and show the confirmation', async () => {
    await setup('abc-123');

    const req = httpTesting.expectOne(`${environment.apiUrl}/auth/validate_user`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'abc-123' });
    req.flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="status"]').textContent).toContain('sucesso');
  });

  it('should show invalid link when the token is rejected', async () => {
    await setup('expirado');

    httpTesting
      .expectOne(`${environment.apiUrl}/auth/validate_user`)
      .flush(null, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('a[href="/login"]')).toBeTruthy();
  });
});
