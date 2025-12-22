package by.egrius.app.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StopWordsUpdateDto(
        @NotBlank String stopWords
) {}