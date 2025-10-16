package by.egrius.app.dto.fileDTO;

import by.egrius.app.entity.enums.Language;

public record FileContentReadDto(
        String rawText,
        Long lineCount,
        Long wordCount,
        Language language
) {}
