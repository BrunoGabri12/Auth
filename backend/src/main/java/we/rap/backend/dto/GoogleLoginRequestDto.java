package we.rap.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDto(
        @NotBlank
        String idToken
) { }
