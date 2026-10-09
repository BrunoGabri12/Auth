package we.rap.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserEditRequestDto(

    @NotBlank
    @Size(min = 3, max = 255)
    String username
) {} 
