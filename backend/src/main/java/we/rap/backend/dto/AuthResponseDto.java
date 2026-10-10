package we.rap.backend.dto;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        // Segundos restantes até a expiração do access token
        long expiresIn
) { }
