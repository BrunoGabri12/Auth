import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { ForgotPasswordPage } from './forgot-password-page';

describe('ForgotPasswordPage', () => {
  let fixture: ComponentFixture<ForgotPasswordPage>;
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForgotPasswordPage],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ForgotPasswordPage);
    httpTesting = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => httpTesting.verify());

  function submit(email: string) {
    const element: HTMLElement = fixture.nativeElement;
    const input = element.querySelector<HTMLInputElement>('input[type="email"]')!;
    input.value = email;
    input.dispatchEvent(new Event('input'));
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('should send the e-mail and show the confirmation', () => {
    submit('user@werap.com');

    const req = httpTesting.expectOne(`${environment.apiUrl}/auth/forgot_password`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'user@werap.com' });
    req.flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="status"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
  });

  it('should not send the request with an invalid e-mail', () => {
    submit('invalid');

    httpTesting.expectNone(`${environment.apiUrl}/auth/forgot_password`);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });
});
