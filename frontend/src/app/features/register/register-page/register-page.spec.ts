import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { RegisterPage } from './register-page';

describe('RegisterPage', () => {
  let fixture: ComponentFixture<RegisterPage>;
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterPage],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterPage);
    httpTesting = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => httpTesting.verify());

  function submit(values: {
    email: string;
    username: string;
    password: string;
    confirmPassword: string;
  }) {
    const element: HTMLElement = fixture.nativeElement;
    const [emailInput, usernameInput, passwordInput, confirmInput] =
      element.querySelectorAll<HTMLInputElement>('input');
    for (const [input, value] of [
      [emailInput, values.email],
      [usernameInput, values.username],
      [passwordInput, values.password],
      [confirmInput, values.confirmPassword],
    ] as const) {
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  const validUser = {
    email: 'user@werap.com',
    username: 'werap',
    password: 'senha-forte',
    confirmPassword: 'senha-forte',
  };

  it('should register the user and show the backend message', () => {
    submit(validUser);

    const req = httpTesting.expectOne(`${environment.apiUrl}/user/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'user@werap.com',
      username: 'werap',
      password: 'senha-forte',
    });
    req.flush({ message: 'Cadastro realizado!' }, { status: 201, statusText: 'Created' });
    fixture.detectChanges();

    const status: HTMLElement = fixture.nativeElement.querySelector('[role="status"]');
    expect(status.textContent).toContain('Cadastro realizado!');
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
  });

  it('should not send the request when passwords do not match', () => {
    submit({ ...validUser, confirmPassword: 'outra-senha' });

    httpTesting.expectNone(`${environment.apiUrl}/user/register`);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });

  it('should not send the request with a short username', () => {
    submit({ ...validUser, username: 'ab' });

    httpTesting.expectNone(`${environment.apiUrl}/user/register`);
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });

  it('should keep the form and show an error when the backend rejects the registration', () => {
    submit(validUser);

    httpTesting
      .expectOne(`${environment.apiUrl}/user/register`)
      .flush(null, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('form')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });
});
