package by.egrius.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FileDeleteRequestDto (
    @NotNull UUID fileId,
    @NotBlank String rawPassword
){}
