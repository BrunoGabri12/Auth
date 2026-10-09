package we.rap.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import we.rap.backend.enums.Roles;

public record UserResponseDto(
    UUID id,
    String email,
    String username,
    boolean emailVerified,
    LocalDateTime createdAt,
    Roles role
) {}
