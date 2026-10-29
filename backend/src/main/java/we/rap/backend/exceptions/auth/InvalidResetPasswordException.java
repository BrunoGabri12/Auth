package we.rap.backend.exceptions.auth;

public class InvalidResetPasswordException extends RuntimeException {
    public InvalidResetPasswordException() {
        super("User doesn't have permission");
    }
}
