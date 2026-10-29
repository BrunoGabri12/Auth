import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../shared/services/auth/auth.service';

@Component({
  imports: [RouterLink],
  selector: 'app-validate-user-page',
  templateUrl: './validate-user-page.html',
})
export class ValidateUserPage implements OnInit {
  private readonly authService = inject(AuthService);

  // Token enviado no link do e-mail: /validate_user?token=...
  private readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token') ?? '';

  protected readonly loading = signal(!!this.token);
  protected readonly done = signal(false);
  protected readonly invalidLink = signal(!this.token);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit() {
    if (!this.token) {
      return;
    }

    this.authService.validateUser({ token: this.token }).subscribe({
      next: () => {
        this.loading.set(false);
        this.done.set(true);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        // 400: token inexistente, expirado ou já utilizado
        if (error.status === 400) {
          this.invalidLink.set(true);
          return;
        }
        this.errorMessage.set('Não foi possível validar o e-mail. Tente novamente.');
      },
    });
  }
}
