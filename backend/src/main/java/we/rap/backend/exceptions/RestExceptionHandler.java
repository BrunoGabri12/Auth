package we.rap.backend.exceptions;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import we.rap.backend.exceptions.auth.InvalidCredentialsException;
import we.rap.backend.exceptions.auth.InvalidEmailValidationException;
import we.rap.backend.exceptions.auth.InvalidRefreshTokenException;
import we.rap.backend.exceptions.auth.InvalidResetPasswordException;
import we.rap.backend.exceptions.auth.InvalidUserTokenException;
import we.rap.backend.exceptions.mail.ErrorToSendException;
import we.rap.backend.exceptions.user.EmailAlreadyInUseException;
import we.rap.backend.exceptions.user.UserNotFoundException;
import we.rap.backend.exceptions.user.UserPermissionException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailInUse(EmailAlreadyInUseException ex) {
        return  build(HttpStatus.BAD_REQUEST, List.of("Não foi possível completar o cadastro"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, List.of("Senha ou email inválidos"));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return build(HttpStatus.UNAUTHORIZED, List.of("Refresh token inválido ou expirado"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, List.of("Conflito com dados já existentes"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = Stream.concat(
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage()),
                ex.getBindingResult().getGlobalErrors().stream()
                        .map(e -> e.getObjectName() + ": " + e.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, List.of("E-mail mal formado"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundUser(UserNotFoundException ex) {
        return build(HttpStatus.OK, List.of("Operação inválida"));
    } 

    @ExceptionHandler({UserPermissionException.class})
    public ResponseEntity<ApiError> handlePermissionException(UserPermissionException ex){
        return build(HttpStatus.FORBIDDEN,List.of("Usuário não tem permissão"));
    }

    @ExceptionHandler({InvalidResetPasswordException.class}) 
    public ResponseEntity<ApiError> handlResponseEntity(InvalidResetPasswordException ex) {
        return build(HttpStatus.FORBIDDEN, List.of("Usuário não tem permissão"));
    }

    @ExceptionHandler(InvalidUserTokenException.class)
    public ResponseEntity<ApiError> handleInvalidUserToken(InvalidUserTokenException ex) {
        return build(HttpStatus.BAD_REQUEST, List.of("Link inválido ou expirado"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, List.of("Um erro inesperado aconteceu"));
    }

    @ExceptionHandler(InvalidEmailValidationException.class)
    public ResponseEntity<ApiError> handleNotValidateUser(Exception ex){
        return build(HttpStatus.FORBIDDEN, List.of("E-mail não validado. Use o link de validação enviado para o seu e-mail"));
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class })
    public ResponseEntity<ApiError> handleResouceNotFound(Exception ex){
        return build(HttpStatus.NOT_FOUND, List.of("Recurso não encontrado"));
    }

    @ExceptionHandler({ErrorToSendException.class}) 
    public ResponseEntity<ApiError> handleNotAvailableEmail() {
        return build(HttpStatus.SERVICE_UNAVAILABLE, List.of("Não foi possível enviar o e-mail. Tente mais tarde"));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, List<String> errors) {
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .code(status.value())
                .status(status.getReasonPhrase())
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
