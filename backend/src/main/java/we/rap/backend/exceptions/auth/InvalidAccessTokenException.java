package we.rap.backend.exceptions.auth;

public class InvalidAccessTokenException extends RuntimeException {

    public InvalidAccessTokenException() {
        super("Invalid or expired access token");
    }
}
