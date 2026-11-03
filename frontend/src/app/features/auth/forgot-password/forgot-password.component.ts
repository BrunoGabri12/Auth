import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../shared/services/auth/auth.service';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-forgot-password',
  styleUrl: './forgot-password.component.scss',
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent {
  private readonly authService = inject(AuthService);

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
  });

  protected readonly loading = signal(false);
  protected readonly sent = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected sendResetLink() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.forgotPassword(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.sent.set(true);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Não foi possível enviar o e-mail. Tente novamente.');
      },
    });
  }
}
