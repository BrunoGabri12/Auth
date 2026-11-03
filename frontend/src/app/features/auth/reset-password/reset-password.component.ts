import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  PASSWORD_MAX_LENGTH,
  PASSWORD_MIN_LENGTH,
  passwordsMatch,
} from '../../../shared/validators/password';
import { AuthService } from '../shared/services/auth/auth.service';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-reset-password',
  styleUrl: './reset-password.component.scss',
  templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent {
  private readonly authService = inject(AuthService);

  // Token enviado no link do e-mail: /reset_password?token=...
  private readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token') ?? '';

  protected readonly passwordMinLength = PASSWORD_MIN_LENGTH;
  protected readonly passwordMaxLength = PASSWORD_MAX_LENGTH;

  protected readonly form = inject(NonNullableFormBuilder).group(
    {
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
  protected readonly done = signal(false);
  protected readonly invalidLink = signal(!this.token);
  protected readonly errorMessage = signal<string | null>(null);

  protected resetPassword() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService
      .resetPassword({ token: this.token, password: this.form.controls.password.value })
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.done.set(true);
        },
        error: (error: HttpErrorResponse) => {
          this.loading.set(false);
          // 403: token inexistente, expirado ou já utilizado
          if (error.status === 403) {
            this.invalidLink.set(true);
            return;
          }
          this.errorMessage.set('Não foi possível redefinir a senha. Tente novamente.');
        },
      });
  }
}
