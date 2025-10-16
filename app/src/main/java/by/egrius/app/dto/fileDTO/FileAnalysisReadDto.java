package by.egrius.app.dto.fileDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record FileAnalysisReadDto (
         UUID id,
         List<String> topWords,
         Map<String, Long> startsWithMap,
         Map<Character, Long> punctuationMap,
         Map<String, Long> wordLengthMap,
         Boolean stopWordsExcluded
) {}
