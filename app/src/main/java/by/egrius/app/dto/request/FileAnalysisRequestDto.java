package by.egrius.app.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record FileAnalysisRequestDto(
        @Min(1) @Max(100) int topN,
        boolean excludeStopWords
) {}