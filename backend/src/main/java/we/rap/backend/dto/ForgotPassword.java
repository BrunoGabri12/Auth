package we.rap.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPassword (
    @NotBlank
    String email
) {}
