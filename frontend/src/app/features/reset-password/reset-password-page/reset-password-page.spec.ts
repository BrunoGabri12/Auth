import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { ResetPasswordPage } from './reset-password-page';

describe('ResetPasswordPage', () => {
  let fixture: ComponentFixture<ResetPasswordPage>;
  let httpTesting: HttpTestingController;

  async function setup(token: string | null) {
    await TestBed.configureTestingModule({
      imports: [ResetPasswordPage],
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

    fixture = TestBed.createComponent(ResetPasswordPage);
    httpTesting = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  }

  afterEach(() => httpTesting.verify());

  function submit(password: string, confirmPassword: string) {
    const element: HTMLElement = fixture.nativeElement;
    const [passwordInput, confirmInput] = element.querySelectorAll<HTMLInputElement>('input');
    passwordInput.value = password;
    passwordInput.dispatchEvent(new Event('input'));
    confirmInput.value = confirmPassword;
    confirmInput.dispatchEvent(new Event('input'));
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('should show invalid link when there is no token', async () => {
    await setup(null);

    expect(fixture.nativeElement.querySelector('form')).toBeNull();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });

  it('should send the token and the new password', async () => {
    await setup('abc-123');

    submit('nova-senha', 'nova-senha');

    const req = httpTesting.expectOne(`${environment.apiUrl}/auth/reset_password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'abc-123', password: 'nova-senha' });
    req.flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="status"]')).toBeTruthy();
  });

  it('should not send the request when passwords do not match', async () => {
    await setup('abc-123');

    submit('nova-senha', 'outra-senha');

    httpTesting.expectNone(`${environment.apiUrl}/auth/reset_password`);
  });

  it('should show invalid link when the token is rejected', async () => {
    await setup('expirado');

    submit('nova-senha', 'nova-senha');

    httpTesting
      .expectOne(`${environment.apiUrl}/auth/reset_password`)
      .flush(null, { status: 403, statusText: 'Forbidden' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('form')).toBeNull();
    expect(fixture.nativeElement.querySelector('a[href="/forgot_password"]')).toBeTruthy();
  });
});
