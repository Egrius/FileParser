package by.egrius.app.service;

import by.egrius.app.dto.fileDTO.RegexMatchReadDto;
import by.egrius.app.entity.PatternMatches;
import by.egrius.app.entity.RegexMatch;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.enums.PatternType;
import by.egrius.app.mapper.RegexMatchReadMapper;
import by.egrius.app.repository.PatternMatchesRepository;
import by.egrius.app.repository.RegexMatchRepository;
import by.egrius.app.repository.UploadedFileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegexMatchService {

    private static final Map<PatternType, Pattern> patternTypes = Map.of(
            PatternType.IP, Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b"),
            PatternType.DATE, Pattern.compile("\\b\\d{2}\\.\\d{2}\\.\\d{4}\\b"),
            PatternType.PHONE, Pattern.compile("\\+375(?:\\s?\\(\\d{2}\\)|\\s\\d{2})\\s?\\d{3}-?\\d{2}-?\\d{2}"),
            PatternType.EMAIL, Pattern.compile("\\b[\\w.-]+@[\\w.-]+\\.\\w{2,4}\\b")
    );

    private final UploadedFileRepository uploadedFileRepository;
    private final RegexMatchRepository regexMatchRepository;
    private final RegexMatchReadMapper regexMatchReadMapper;
    private final PatternMatchesRepository patternMatchesRepository;

    @Transactional
    public RegexMatchReadDto createRegexMatch(UUID fileId, Set<PatternType> types) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("Файл не найден"));

        Optional<RegexMatch> existingMatch = regexMatchRepository.findByUploadedFileId(fileId);
        if (existingMatch.isPresent()) {
            regexMatchRepository.delete(existingMatch.get());
            regexMatchRepository.flush();
        }

        String rawText = file.getFileContent().getRawText();
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("Файл не содержит текста для анализа");
        }

        RegexMatch regexMatch = new RegexMatch();
        regexMatch.setUploadedFile(file);

        List<PatternMatches> allPatternMatches = new ArrayList<>();
        long totalMatches = 0;

        for (PatternType type : types) {
            Pattern pattern = patternTypes.get(type);
            if (pattern == null) continue;

            List<String> matches = pattern.matcher(rawText)
                    .results()
                    .map(MatchResult::group)
                    .distinct()
                    .toList();

            for (String matchValue : matches) {
                PatternMatches patternMatch = PatternMatches.builder()
                        .patternType(type)
                        .match(matchValue)
                        .regexMatch(regexMatch)
                        .build();
                allPatternMatches.add(patternMatch);
            }

            totalMatches += matches.size();
        }

        regexMatch.setPatternMatches(allPatternMatches);
        regexMatch.setTotalMatches(totalMatches);

        regexMatchRepository.save(regexMatch);
        regexMatchRepository.flush();

        return regexMatchReadMapper.map(regexMatch);
    }

    @Transactional(readOnly = true)
    public Optional<RegexMatchReadDto> getRegexMatchByFileId(UUID fileId) {
        return regexMatchRepository.findByUploadedFileId(fileId)
                .map(regexMatchReadMapper::map);
    }

    @Transactional(readOnly = true)
    public List<PatternMatches> getPatternMatchesByType(UUID fileId, PatternType type) {
        return patternMatchesRepository.findByRegexMatchUploadedFileIdAndPatternType(fileId, type);
    }

    @Transactional
    public void deleteRegexMatch(UUID fileId) {
        regexMatchRepository.findByUploadedFileId(fileId)
                .ifPresent(regexMatchRepository::delete);

    }
}
