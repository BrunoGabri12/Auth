package we.rap.backend.exceptions.mail;

public class ErrorToSendException extends RuntimeException {
    public ErrorToSendException() {
        super("Unable to send the e-mail");
    }
}
