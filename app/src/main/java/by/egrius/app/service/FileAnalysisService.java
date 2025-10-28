package by.egrius.app.service;

import by.egrius.app.dto.fileDTO.FileAnalysisReadDto;
import by.egrius.app.entity.FileAnalysis;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.enums.FileEventType;
import by.egrius.app.publisher.FileEventPublisher;
import by.egrius.app.repository.FileAnalysisRepository;
import by.egrius.app.repository.UploadedFileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileAnalysisService {

    private final FileAnalysisRepository fileAnalysisRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final FileEventPublisher fileEventPublisher;

    @Value("${text.analysis.stopwords}")
    private String stopWordsRaw;

    @Transactional
    public FileAnalysisReadDto createAnalysis(UUID fileId, int topN, boolean stopWordsExcluded) {

        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("Не найден файл для создания анализа"));

        // Возможно сделать логику пересоздания анализа...
        if (uploadedFile.getFileAnalysis() != null) {
            throw new IllegalStateException("Анализ уже существует для этого файла");
        }

        fileEventPublisher.publish(FileEventType.PARSE_START, fileId);

        String rawText = uploadedFile.getFileContent().getRawText();
        List<String> words = extractWords(rawText, stopWordsExcluded);

        Map<String, Long> topWords = countTopWords(words, topN);
        Map<Character, Long> startsWithMap = startsWithCount(words);
        Map<Character, Long> punctuationMap = punctuationCount(rawText);
        Map<String, Integer> wordLengthMap = wordLengthCount(words);

        FileAnalysis analysis = buildAnalysis(uploadedFile, topWords, startsWithMap, punctuationMap, wordLengthMap, stopWordsExcluded);
        fileAnalysisRepository.save(analysis);

        fileEventPublisher.publish(FileEventType.PARSE_END, fileId);

        return new FileAnalysisReadDto(
                analysis.getId(),
                topWords,
                startsWithMap,
                punctuationMap,
                wordLengthMap,
                stopWordsExcluded
        );
    }

    public void setStopWordsRaw(String stopWordsRaw) {
        this.stopWordsRaw = stopWordsRaw;
    }

    private FileAnalysis buildAnalysis(UploadedFile file,
                                       Map<String, Long> topWords,
                                       Map<Character, Long> startsWithMap,
                                       Map<Character, Long> punctuationMap,
                                       Map<String, Integer> wordLengthMap,
                                       boolean stopWordsExcluded) {
        return FileAnalysis.builder()
                .uploadedFile(file)
                .topWords(topWords)
                .startsWithMap(startsWithMap)
                .punctuationMap(punctuationMap)
                .wordLengthMap(wordLengthMap)
                .stopWordsExcluded(stopWordsExcluded)
                .build();
    }

    private Map<String, Integer> wordLengthCount(List<String> words) {
        return words.stream()
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        String::length
                ));
    }

    private Map<Character, Long> punctuationCount(String rawText) {
        return rawText
                .chars()
                .mapToObj(c -> (char) c)
                .filter(c -> String.valueOf(c).matches("\\p{Punct}"))
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }

    private Map<Character, Long> startsWithCount(List<String> words) {
        return words.stream()
                .filter(w -> !w.isBlank())
                .map(w -> w.charAt(0))
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }

    private Map<String, Long> countTopWords(List<String> words, int topN) {
        return words.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private List<String> extractWords(String rawText, boolean stopWordsExcluded) {
        Stream<String> stream = Arrays.stream(rawText.split("\\s+"))
                .map(this::normalizeWord)
                .filter(w -> !w.isBlank());

        if (stopWordsExcluded) {
            Set<String> stopWords = getStopWords();
            stream = stream.filter(w -> !stopWords.contains(w));
        }

        return stream.toList();
    }
    // Утилитные методы

    private Map<String, Long> topWordsCount(List<String> words) {
        return words.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }


    private String normalizeWord(String word) {
        return  word.replaceAll("[\\p{Punct}&&[^-]]", "").toLowerCase().trim();
    }

    private Set<String> getStopWords() {
        return Arrays.stream(stopWordsRaw.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
