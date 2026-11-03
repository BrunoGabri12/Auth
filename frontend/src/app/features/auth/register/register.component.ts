import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../shared/services/user/user.service';
import {
  PASSWORD_MAX_LENGTH,
  PASSWORD_MIN_LENGTH,
  passwordsMatch,
} from '../../../shared/validators/password';

// Mesmos limites do UserRequestDto no backend
const USERNAME_MIN_LENGTH = 3;
const USERNAME_MAX_LENGTH = 255;
const EMAIL_MAX_LENGTH = 255;

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-register',
  styleUrl: './register.component.scss',
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private readonly userService = inject(UserService);

  protected readonly usernameMinLength = USERNAME_MIN_LENGTH;
  protected readonly usernameMaxLength = USERNAME_MAX_LENGTH;
  protected readonly passwordMinLength = PASSWORD_MIN_LENGTH;
  protected readonly passwordMaxLength = PASSWORD_MAX_LENGTH;

  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      email: ['', [Validators.required, Validators.email, Validators.maxLength(EMAIL_MAX_LENGTH)]],
      username: [
        '',
        [
          Validators.required,
          Validators.minLength(USERNAME_MIN_LENGTH),
          Validators.maxLength(USERNAME_MAX_LENGTH),
        ],
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(PASSWORD_MIN_LENGTH),
          Validators.maxLength(PASSWORD_MAX_LENGTH),
        ],
      ],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch }
  );

  protected readonly loading = signal(false);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected register() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const { email, username, password } = this.form.getRawValue();

    this.userService.register({ email, username, password }).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.successMessage.set(response.message);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(registerErrorMessage(error.status));
      },
    });
  }
}

function registerErrorMessage(status: number): string {
  switch (status) {
    // 400: dados inválidos ou e-mail já cadastrado; o backend não diferencia de propósito
    case 400:
      return 'Não foi possível concluir o cadastro. Verifique os dados ou use outro e-mail.';
    // 503: falha ao enviar o e-mail de validação; o cadastro é desfeito e pode ser repetido
    case 503:
      return 'Não foi possível enviar o e-mail de confirmação. Tente novamente mais tarde.';
    default:
      return 'Não foi possível concluir o cadastro. Tente novamente.';
  }
}
