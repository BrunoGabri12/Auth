import { Routes } from "@angular/router";
import { LoginComponent } from "./login/login.component";
import { RegisterComponent } from "./register/register.component";
import { ForgotPasswordComponent } from "./forgot-password/forgot-password.component";
import { ResetPasswordComponent } from "./reset-password/reset-password.component";
import { ValidateUserComponent } from "./validate-user/validate-user.component";

export const AUTH_ROUTES: Routes = [ 
    { path: '', pathMatch: 'full',redirectTo:'login'},
    { path: 'login', component: LoginComponent},
    { path: 'register', component: RegisterComponent},
    { path: 'forgot_password', component: ForgotPasswordComponent},
    { path: 'reset_password', component: ResetPasswordComponent},
    { path: 'validate_user', component: ValidateUserComponent}
]