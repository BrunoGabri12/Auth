package we.rap.backend.exceptions.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("User doesn't found or inactive " + id);
    }
}
