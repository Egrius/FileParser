package by.egrius.app.unit.service;

import by.egrius.app.dto.fileDTO.FileAnalysisReadDto;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.repository.FileAnalysisRepository;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.service.FileAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileAnalysisServiceUnitTest {
    @Mock
    private FileAnalysisRepository fileAnalysisRepository;

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @InjectMocks
    private FileAnalysisService fileAnalysisService;

    @Test
    void createAnalysis_ShouldReturnCorrectDataSet() {
        UUID fileId = UUID.randomUUID();

        String rawText = "Привет, мир! Это тестовый текст и что-то ещё.";

        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .user(User.builder().userId(UUID.randomUUID()).build())
                .fileContent(FileContent.builder().rawText(rawText).build())
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));
        when(fileAnalysisRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        fileAnalysisService.setStopWordsRaw("и,что,это");

        FileAnalysisReadDto result = fileAnalysisService.createAnalysis(fileId, 5, true);

        System.out.println("Top words: " + result.topWords());

        System.out.println("Starts with map:");
        printFormattedMap(result.startsWithMap());

        System.out.println("Punctuation:");
        printFormattedMap(result.punctuationMap());

        System.out.println("Words length map:");
        printFormattedMap(result.wordLengthMap());



        // Проверка: исключены стоп-слова
        assertFalse(result.topWords().containsKey("и"));
        assertFalse(result.topWords().containsKey("что"));
        assertFalse(result.topWords().containsKey("это"));

        // Проверка: частотный словарь
        assertEquals(1L, result.topWords().get("привет"));
        assertEquals(1L, result.topWords().get("мир"));
        assertEquals(1L, result.topWords().get("тестовый"));
        assertEquals(1L, result.topWords().get("текст"));

        // Проверка: пунктуация
        assertEquals(1L, result.punctuationMap().get('!'));
        assertEquals(1L, result.punctuationMap().get('.'));

        // Проверка: начальные буквы
        assertEquals(1L, result.startsWithMap().get('п')); // привет
        assertEquals(1L, result.startsWithMap().get('м')); // мир
        assertEquals(2L, result.startsWithMap().get('т')); // тестовый, текст
        assertEquals(1L, result.startsWithMap().get('е')); // ещё

        // Проверка: длина слов
        assertEquals(6, result.wordLengthMap().get("привет"));
        assertEquals(8, result.wordLengthMap().get("тестовый"));
        assertEquals(5, result.wordLengthMap().get("текст"));

        // Проверка: флаг стоп-слов
        assertTrue(result.stopWordsExcluded());

    }

    private <K extends Comparable<K>, V extends Comparable<V>> void printFormattedMap(Map<K, V> mapToPrint) {
        for(Map.Entry<K,V> entry : mapToPrint.entrySet()) {
            System.out.println(entry.getKey().toString() + " : " + entry.getValue().toString());
        }
    }
}