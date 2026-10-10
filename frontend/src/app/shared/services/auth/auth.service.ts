import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, EMPTY, finalize, Observable, shareReplay, tap, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { UserCredentials } from './dto/user-credentials.dto';
import { StorageService } from '../storage/storage.service';
import { Token } from './dto/token.dto';
import { RefreshScheduleService } from '../refresh-schedule/refresh-schedule.service';

const STORAGE_REFRESH = 'werap.auth.refresh'
const STORAGE_ACCESS = 'werap.auth.access'

@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storageService = inject(StorageService);
  private readonly refreshSchedule = inject(RefreshScheduleService);

  private readonly baseUrl = `${environment.apiUrl}/auth`;

  // Refresh em andamento, compartilhado entre chamadas concorrentes
  private refreshInProgress$?: Observable<Token>;

  login(userCredentials: UserCredentials):Observable<Token> {
    return this.http.post<Token>(`${this.baseUrl}/login`, userCredentials)
      .pipe(
        tap((tokens) => this.saveTokensAndStartSchedule(tokens))
      )
  }

  refresh():Observable<Token>{
    if(this.refreshInProgress$) return this.refreshInProgress$;

    const refreshToken = this.storageService.get(STORAGE_REFRESH)
    if(!refreshToken) return throwError(() => new Error("Refresh Token não encontrado"));

    this.refreshInProgress$ = this.http.post<Token>(`${this.baseUrl}/refresh`, {refreshToken})
      .pipe(
        tap((tokens) => this.saveTokensAndStartSchedule(tokens)),
        finalize(() => this.refreshInProgress$ = undefined),
        shareReplay({ bufferSize: 1, refCount: false })
      );

    return this.refreshInProgress$;
  }

  getAccessToken(): string | null {
    return this.storageService.get(STORAGE_ACCESS);
  }

  logout(){
    this.refreshSchedule.removeSchedule();
    const refreshToken = this.storageService.get(STORAGE_REFRESH)

    this.http.post<void>(`${this.baseUrl}/logout`, {refreshToken})
      .pipe(
        catchError(() => EMPTY),
        finalize(() => this.storageService.clearAll())
      )
      .subscribe();
  }

  private saveTokensAndStartSchedule(tokens:Token) {
    this.storageTokens(tokens)
    this.startSchedule(tokens.expiresIn);
  }

  private storageTokens(token:Token) {
    this.storageService.setInBatch([
      {
        key: STORAGE_REFRESH,
        value: token.refreshToken
      },
      {
        key: STORAGE_ACCESS,
        value: token.accessToken
      }
    ])
  }

  private startSchedule(expiresIn:number) {
    this.refreshSchedule.init(expiresIn, () =>
      this.refresh().pipe(
        catchError(() => {
          this.logout();
          return EMPTY;
        })
      )
    );
  }
}
