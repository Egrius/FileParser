package by.egrius.app.dto.fileDTO;

public record FileStatsDto(
        UploadedFileReadDto file,
        FileAnalysisReadDto analysis,
        RegexMatchReadDto regexMatch,
        boolean hasAnalysis,
        boolean hasRegexMatches
) {}