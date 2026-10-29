package we.rap.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateToken(
    @NotBlank 
    String token
) {} 
