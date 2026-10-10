import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { User } from './dto/user.dto';

@Service()
export class UserService {
  private readonly http = inject(HttpClient);

  private readonly baseUrl = `${environment.apiUrl}/user`;

  getMe(): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/me`);
  }
}
