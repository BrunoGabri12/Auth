import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../shared/services/auth/auth.service';
import { GoogleSigninButtonDirective, SocialAuthService, SocialUser } from '@abacritt/angularx-social-login';

@Component({
  imports: [ReactiveFormsModule, GoogleSigninButtonDirective],
  selector: 'app-login-page',
  styleUrl: './login-page.scss',
  templateUrl: './login-page.html',
})
export class LoginPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly socialAuthService = inject(SocialAuthService);

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.socialAuthService.authState.subscribe((user) => {
      console.log(user);
      if(user.idToken) {
        this.authService.loginWithGoogle(user.idToken).subscribe({
          next: () => this.router.navigate(['/home']),
          error: () => this.errorMessage.set("FALHA")
        });
      }
    })
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
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('E-mail ou senha inválidos');
      },
    });
  }
}
