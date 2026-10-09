package we.rap.backend.dto;

import java.util.UUID;

public record AccessTokenClaims(
        UUID userId,
        UUID sessionId
) { }
