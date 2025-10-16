package by.egrius.app.dto.fileDTO;

import by.egrius.app.entity.enums.PatternType;

import java.util.List;

public record RegexMatchReadDto(
        PatternType patternType,
        List<String> matches,
        Long matchCount
) {}
