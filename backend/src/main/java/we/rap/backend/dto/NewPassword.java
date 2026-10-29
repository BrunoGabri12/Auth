package we.rap.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewPassword(

    @NotBlank 
    String token,

    @NotBlank
    @Size(min = 8, max = 72)
    String password
) { }
