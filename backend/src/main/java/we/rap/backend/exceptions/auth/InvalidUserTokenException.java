package we.rap.backend.exceptions.auth;

public class InvalidUserTokenException extends RuntimeException {
    public InvalidUserTokenException() {
        super("Token inválido ou expirado");
    }
}
