package by.egrius.app.dto.fileDTO;

import by.egrius.app.entity.enums.PatternType;

import java.util.List;
import java.util.Map;

public record RegexMatchReadDto(
        List<String> emailMatches,
        List<String> phoneMatches,
        List<String> ipMatches,
        List<String> dateMatches,
        Long matchCount
) {}
