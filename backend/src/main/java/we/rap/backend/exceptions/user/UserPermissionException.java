package we.rap.backend.exceptions.user;

import java.util.UUID;

public class UserPermissionException extends RuntimeException {
     public UserPermissionException(UUID id) {
        super("User doesn't have permission" + id);
    }
}
