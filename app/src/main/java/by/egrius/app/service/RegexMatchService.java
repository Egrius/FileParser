package by.egrius.app.service;

import by.egrius.app.dto.fileDTO.RegexMatchReadDto;
import by.egrius.app.entity.RegexMatch;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.enums.PatternType;
import by.egrius.app.mapper.RegexMatchReadMapper;
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

    @Transactional
    public RegexMatchReadDto createRegexMatch(UUID fileId, Set<PatternType> types) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("Файл не найден"));

        String rawText = file.getFileContent().getRawText();

        Map<PatternType, List<String>> matchesByType = new EnumMap<>(PatternType.class);
        long totalMatches = 0;

        for (PatternType type : types) {
            Pattern pattern = patternTypes.get(type);
            if (pattern == null) continue;

            List<String> matches = pattern.matcher(rawText)
                    .results()
                    .map(MatchResult::group)
                    .distinct()
                    .toList();

            matchesByType.put(type, matches);
            totalMatches += matches.size();
        }

        RegexMatch match = new RegexMatch();      // вручную, не через @Builder
        match.setUploadedFile(file);              // критично: установить до persist
        match.setMatchesByType(matchesByType);
        match.setTotalMatches(totalMatches);

        // отладка перед сохранением
        System.out.println("file.id = " + file.getId());
        System.out.println("match.id before save = " + match.getId()); // должен быть равен file.getId() или не null после setUploadedFile

        regexMatchRepository.save(match);
        regexMatchRepository.flush();


        return regexMatchReadMapper.map(match);
    }
}
