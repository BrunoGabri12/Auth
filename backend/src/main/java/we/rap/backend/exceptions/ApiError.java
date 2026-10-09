package we.rap.backend.exceptions;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ApiError (
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "UTC")
    Instant timestamp,

    Integer code,

    String status,

    List<String> errors
) {
}
