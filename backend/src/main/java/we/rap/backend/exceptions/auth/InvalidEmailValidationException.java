package we.rap.backend.exceptions.auth;

public class InvalidEmailValidationException extends RuntimeException {
    
    public InvalidEmailValidationException() {
        super("A conta não possui o e-mail validado ainda");
    }
}
