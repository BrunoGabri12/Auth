import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { tokenInterceptor } from './shared/interceptors/token-interceptor/token-interceptor';
import { GoogleInitOptions, GoogleLoginProvider, SOCIAL_AUTH_CONFIG, SocialAuthServiceConfig } from '@abacritt/angularx-social-login';
import { environment } from '../environments/environment';

const googleInitOptions: GoogleInitOptions = {
  oneTapEnabled: false,
}


export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([tokenInterceptor])),
    {
      provide: SOCIAL_AUTH_CONFIG,
      useValue: {
        autoLogin: false, 
        providers: [
          {
            id: GoogleLoginProvider.PROVIDER_ID,
            provider: new GoogleLoginProvider(
              environment.googleProviderId,
              googleInitOptions
            )
          }
        ],
        onError: (err: any) => {
          console.error(err);
        },
      } as SocialAuthServiceConfig
    },
  ],
};
