package by.egrius.app.service;

import by.egrius.app.dto.fileDTO.FileAnalysisReadDto;
import by.egrius.app.entity.FileAnalysis;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.enums.FileEventType;
import by.egrius.app.mapper.fileMapper.FileAnalysisReadMapper;
import by.egrius.app.publisher.FileEventPublisher;
import by.egrius.app.repository.FileAnalysisRepository;
import by.egrius.app.repository.UploadedFileRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileAnalysisService {

    private final FileAnalysisRepository fileAnalysisRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final FileEventPublisher fileEventPublisher;
    private final FileAnalysisReadMapper fileAnalysisReadMapper;

    @Value("${text.analysis.stopwords:}")
    private String defaultStopWordsRaw;

    private String currentStopWordsRaw;

    @PostConstruct
    public void init() {
        this.currentStopWordsRaw = defaultStopWordsRaw;
    }

    @Transactional
    public FileAnalysisReadDto createAnalysis(UUID fileId, int topN, boolean stopWordsExcluded) {

        if (topN <= 0) {
            throw new IllegalArgumentException("topN должен быть положительным числом");
        }

        UploadedFile uploadedFile = uploadedFileRepository.findWithFileAnalysisById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("Не найден файл для создания анализа"));

        if (uploadedFile.getFileContent() == null ||
                uploadedFile.getFileContent().getRawText() == null) {
            throw new IllegalStateException("Файл не содержит текста для анализа");
        }

        // Возможно сделать логику пересоздания анализа...
        if (uploadedFile.getFileAnalysis() != null) {
            throw new IllegalStateException("Анализ уже существует для этого файла");
        }

        fileEventPublisher.publish(FileEventType.PARSE_START, fileId);

        String rawText = uploadedFile.getFileContent().getRawText();

        if (rawText.isBlank()) {
            throw new IllegalStateException("Текст файла пустой");
        }

        List<String> words = extractWords(rawText, stopWordsExcluded);

        Map<String, Long> topWords = countTopWords(words, topN);
        Map<Character, Long> startsWithMap = startsWithCount(words);
        Map<Character, Long> punctuationMap = punctuationCount(rawText);
        Map<String, Integer> wordLengthMap = wordLengthCount(words);

        log.info("Создание анализа для файла {}: topN={}, stopWordsExcluded={}", fileId, topN, stopWordsExcluded);

        FileAnalysis analysis = buildAnalysis(uploadedFile, topWords, startsWithMap, punctuationMap, wordLengthMap, stopWordsExcluded);

        log.info("Анализ создан для файла {}. Найдено {} уникальных слов, топ слов: {}", fileId, words.size(), topWords.size());

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

    public Optional<FileAnalysisReadDto> getAnalysisByFileId(UUID fileId) {
        return fileAnalysisRepository.findByUploadedFile_Id(fileId)
                .map(fileAnalysisReadMapper::map);
    }

    public void setStopWordsRaw(String stopWordsRaw) {
        this.currentStopWordsRaw = stopWordsRaw;
    }

    private FileAnalysis buildAnalysis(UploadedFile file,
                                       Map<String, Long> topWords,
                                       Map<Character, Long> startsWithMap,
                                       Map<Character, Long> punctuationMap,
                                       Map<String, Integer> wordLengthMap,

                                       boolean stopWordsExcluded) {

        FileAnalysis analysis = FileAnalysis.builder()
                .uploadedFile(file)
                .topWords(topWords)
                .startsWithMap(startsWithMap)
                .punctuationMap(punctuationMap)
                .wordLengthMap(wordLengthMap)
                .stopWordsExcluded(stopWordsExcluded)
                .build();

        // Устанавливаем обратную связь
        file.setFileAnalysis(analysis);

        return analysis;
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

    private String normalizeWord(String word) {
        return  word.replaceAll("[\\p{Punct}&&[^-]]", "").toLowerCase().trim();
    }

    private Set<String> getStopWords() {
        if (currentStopWordsRaw == null || currentStopWordsRaw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(currentStopWordsRaw.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
