import { AbstractControl, ValidationErrors } from '@angular/forms';

// Mesmos limites do UserRequestDto e do NewPassword no backend
export const PASSWORD_MIN_LENGTH = 8;
export const PASSWORD_MAX_LENGTH = 72;

// Validator de grupo: espera os controles password e confirmPassword
export function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const { password, confirmPassword } = group.value;
  return password === confirmPassword ? null : { passwordsMismatch: true };
}
