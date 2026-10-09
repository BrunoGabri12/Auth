package we.rap.backend.dto;

import java.time.LocalDateTime;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        LocalDateTime expiresAt
) { }
