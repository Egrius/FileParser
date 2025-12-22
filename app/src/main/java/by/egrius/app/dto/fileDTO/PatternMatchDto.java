package by.egrius.app.dto.fileDTO;

import by.egrius.app.entity.enums.PatternType;

public record PatternMatchDto(
        PatternType patternType,
        String match
) {}