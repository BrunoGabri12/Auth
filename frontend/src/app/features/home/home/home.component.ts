import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { UserService } from '../../../shared/services/user/user.service';
import { User } from '../../../shared/services/user/dto/user.dto';
import { AuthService } from '../../auth/shared/services/auth/auth.service';

@Component({
  imports: [DatePipe],
  selector: 'app-home',
  styleUrl: './home.component.scss',
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly user = signal<User | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.userService.getMe().subscribe({
      next: (user) => this.user.set(user),
      error: () => this.errorMessage.set('Não foi possível carregar os dados do usuário'),
    });
  }

  protected logout() {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
