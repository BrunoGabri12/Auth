import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { filter, finalize, map } from 'rxjs';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../shared/services/auth/auth.service';
import { GoogleSigninButtonDirective, SocialAuthService } from '@abacritt/angularx-social-login';

@Component({
  imports: [ReactiveFormsModule, RouterLink, GoogleSigninButtonDirective],
  selector: 'app-login-page',
  styleUrl: './login-page.scss',
  templateUrl: './login-page.html',
})
export class LoginPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly socialAuthService = inject(SocialAuthService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.socialAuthService.authState
      .pipe(
        map((user) => user?.idToken),
        filter((idToken): idToken is string => !!idToken),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((idToken) => this.loginWithGoogle(idToken));
  }

  private loginWithGoogle(idToken: string) {
    this.authService
      .loginWithGoogle(idToken)
      .pipe(
        // A sessão do Google só serve para obter o idToken. O authState é um ReplaySubject:
        // sem o signOut ele reemitiria o usuário ao voltar para esta página e refaria o login
        finalize(() => this.socialAuthService.signOut().catch(() => undefined))
      )
      .subscribe({
        next: () => this.router.navigate(['/home']),
        error: () => this.errorMessage.set('Não foi possível entrar com o Google'),
      });
  }

  protected login() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/home']);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(loginErrorMessage(error.status));
      },
    });
  }
}

function loginErrorMessage(status: number): string {
  switch (status) {
    case 401:
      return 'E-mail ou senha inválidos';
    // 403: e-mail ainda não validado; o backend reenvia o link se o último já for antigo
    case 403:
      return 'Seu e-mail ainda não foi validado. Use o link que enviamos para a sua caixa de entrada.';
    default:
      return 'Não foi possível entrar. Tente novamente.';
  }
}
