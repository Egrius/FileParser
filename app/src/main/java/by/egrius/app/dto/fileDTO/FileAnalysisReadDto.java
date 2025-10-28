package by.egrius.app.dto.fileDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record FileAnalysisReadDto (
         UUID id,
         Map<String, Long> topWords,
         Map<Character, Long> startsWithMap,
         Map<Character, Long> punctuationMap,
         Map<String, Integer> wordLengthMap,
         Boolean stopWordsExcluded
) {}
