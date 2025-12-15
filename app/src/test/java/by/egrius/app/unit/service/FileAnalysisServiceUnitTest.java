package by.egrius.app.unit.service;

import by.egrius.app.dto.fileDTO.FileAnalysisReadDto;
import by.egrius.app.entity.FileAnalysis;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.FileEventType;
import by.egrius.app.mapper.fileMapper.FileAnalysisReadMapper;
import by.egrius.app.publisher.FileEventPublisher;
import by.egrius.app.repository.FileAnalysisRepository;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.service.FileAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private FileEventPublisher fileEventPublisher;

    @Mock
    private FileAnalysisReadMapper fileAnalysisReadMapper;

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

        verify(uploadedFileRepository).findById(fileId);
        verify(fileAnalysisRepository).save(any(FileAnalysis.class));
        verify(fileEventPublisher).publish(FileEventType.PARSE_START, fileId);
        verify(fileEventPublisher).publish(FileEventType.PARSE_END, fileId);
    }

    private <K extends Comparable<K>, V extends Comparable<V>> void printFormattedMap(Map<K, V> mapToPrint) {
        for(Map.Entry<K,V> entry : mapToPrint.entrySet()) {
            System.out.println(entry.getKey().toString() + " : " + entry.getValue().toString());
        }
    }

    @Test
    void createAnalysis_ShouldThrowWhenFileNotFound() {

        UUID fileId = UUID.randomUUID();
        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> fileAnalysisService.createAnalysis(fileId, 5, true));

        verify(uploadedFileRepository).findById(fileId);
        verify(fileAnalysisRepository, never()).save(any());
        verify(fileEventPublisher, never()).publish(any(), any());
    }

    @Test
    void createAnalysis_ShouldThrowWhenNoTextContent() {

        UUID fileId = UUID.randomUUID();
        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(null)
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));

        assertThrows(IllegalStateException.class,
                () -> fileAnalysisService.createAnalysis(fileId, 5, true));
    }

    @Test
    void createAnalysis_ShouldThrowWhenTextIsBlank() {

        UUID fileId = UUID.randomUUID();
        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(FileContent.builder().rawText("   ").build())
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));

        assertThrows(IllegalStateException.class,
                () -> fileAnalysisService.createAnalysis(fileId, 5, true));
    }

    @Test
    void createAnalysis_ShouldThrowWhenAnalysisAlreadyExists() {

        UUID fileId = UUID.randomUUID();
        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(FileContent.builder().rawText("текст").build())
                .fileAnalysis(new FileAnalysis())
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));


        assertThrows(IllegalStateException.class,
                () -> fileAnalysisService.createAnalysis(fileId, 5, true));
    }


    @Test
    void createAnalysis_ShouldThrowWhenTopNIsInvalid() {

        UUID fileId = UUID.randomUUID();
        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(FileContent.builder().rawText("текст").build())
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> fileAnalysisService.createAnalysis(fileId, 0, true));

        assertThrows(IllegalArgumentException.class,
                () -> fileAnalysisService.createAnalysis(fileId, -5, true));
    }

    @Test
    void createAnalysis_ShouldWorkWithoutStopWords() {

        UUID fileId = UUID.randomUUID();
        String rawText = "Привет мир";

        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(FileContent.builder().rawText(rawText).build())
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));
        when(fileAnalysisRepository.save(any(FileAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FileAnalysisReadDto result = fileAnalysisService.createAnalysis(fileId, 5, false);

        assertEquals(2, result.topWords().size());
        assertEquals(1L, result.topWords().get("привет"));
        assertEquals(1L, result.topWords().get("мир"));
        assertFalse(result.stopWordsExcluded());
    }

    @Test
    void getAnalysisByFileId_ShouldReturnDtoWhenExists() {

        UUID fileId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();

        FileAnalysis analysis = FileAnalysis.builder()
                .id(analysisId)
                .topWords(Map.of("test", 1L))
                .build();

        FileAnalysisReadDto expectedDto = new FileAnalysisReadDto(
                analysisId,
                Map.of("test", 1L),
                Map.of(),
                Map.of(),
                Map.of(),
                true
        );

        when(fileAnalysisRepository.findByUploadedFile_Id(fileId)).thenReturn(Optional.of(analysis));
        when(fileAnalysisReadMapper.map(analysis)).thenReturn(expectedDto);

        Optional<FileAnalysisReadDto> result = fileAnalysisService.getAnalysisByFileId(fileId);

        assertTrue(result.isPresent());
        assertEquals(expectedDto, result.get());
        verify(fileAnalysisRepository).findByUploadedFile_Id(fileId);
        verify(fileAnalysisReadMapper).map(analysis);
    }

    @Test
    void getAnalysisByFileId_ShouldReturnEmptyWhenNotFound() {

        UUID fileId = UUID.randomUUID();
        when(fileAnalysisRepository.findByUploadedFile_Id(fileId)).thenReturn(Optional.empty());

        Optional<FileAnalysisReadDto> result = fileAnalysisService.getAnalysisByFileId(fileId);

        assertTrue(result.isEmpty());
        verify(fileAnalysisRepository).findByUploadedFile_Id(fileId);
        verify(fileAnalysisReadMapper, never()).map(any());
    }


    @Test
    void createAnalysis_ShouldHandleSpecialCharacters() {
        // Arrange
        UUID fileId = UUID.randomUUID();
        String rawText = "Hello-world test@email.com 123-456-7890";

        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(FileContent.builder().rawText(rawText).build())
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));
        when(fileAnalysisRepository.save(any(FileAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FileAnalysisReadDto result = fileAnalysisService.createAnalysis(fileId, 10, false);

        // Assert
        // Дефис должен сохраниться (согласно вашей regex)
        assertTrue(result.topWords().containsKey("hello-world"));
        // @ должен быть удален
        assertTrue(result.topWords().containsKey("testemailcom"));
        // Дефисы в номере должны сохраниться
        assertTrue(result.topWords().containsKey("123-456-7890"));
    }

    @Test
    void createAnalysis_ShouldHandleEmptyStopWords() {

        UUID fileId = UUID.randomUUID();
        String rawText = "тест текст";

        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(FileContent.builder().rawText(rawText).build())
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));
        when(fileAnalysisRepository.save(any(FileAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act - устанавливаем пустые стоп-слова
        fileAnalysisService.setStopWordsRaw("");
        FileAnalysisReadDto result = fileAnalysisService.createAnalysis(fileId, 5, true);

        // Assert - все слова должны остаться
        assertEquals(2, result.topWords().size());
        assertTrue(result.topWords().containsKey("тест"));
        assertTrue(result.topWords().containsKey("текст"));
    }

    @Test
    void createAnalysis_ShouldHandleNullStopWords() {

        UUID fileId = UUID.randomUUID();
        String rawText = "тест текст";

        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(FileContent.builder().rawText(rawText).build())
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));
        when(fileAnalysisRepository.save(any(FileAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        fileAnalysisService.setStopWordsRaw(null);
        FileAnalysisReadDto result = fileAnalysisService.createAnalysis(fileId, 5, true);

        // Assert - все слова должны остаться
        assertEquals(2, result.topWords().size());
        assertTrue(result.topWords().containsKey("тест"));
        assertTrue(result.topWords().containsKey("текст"));
    }

    @Test
    void createAnalysis_ShouldRespectTopNParameter() {

        UUID fileId = UUID.randomUUID();
        String rawText = "один два два три три три четыре четыре четыре четыре";

        UploadedFile mockFile = UploadedFile.builder()
                .id(fileId)
                .fileContent(FileContent.builder().rawText(rawText).build())
                .build();

        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));
        when(fileAnalysisRepository.save(any(FileAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FileAnalysisReadDto result = fileAnalysisService.createAnalysis(fileId, 2, false);

        // Assert - только 2 самых частых слова
        assertEquals(2, result.topWords().size());
        assertTrue(result.topWords().containsKey("четыре")); // 4 раза
        assertTrue(result.topWords().containsKey("три"));    // 3 раза
        assertFalse(result.topWords().containsKey("два"));   // 2 раза (не входит в top2)
        assertFalse(result.topWords().containsKey("один"));  // 1 раз

        assertEquals(4L, result.topWords().get("четыре"));
        assertEquals(3L, result.topWords().get("три"));
    }
}