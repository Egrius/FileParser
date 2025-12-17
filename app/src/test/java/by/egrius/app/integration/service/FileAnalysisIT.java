package by.egrius.app.integration.service;

import by.egrius.app.dto.fileDTO.FileAnalysisReadDto;
import by.egrius.app.entity.FileAnalysis;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.repository.FileAnalysisRepository;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.FileAnalysisService;
import by.egrius.app.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({
        ServiceTestConfig.class,
        FileAnalysisService.class
})
class FileAnalysisIT {
    @Autowired
    private FileAnalysisRepository fileAnalysisRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileAnalysisService fileAnalysisService;

    private User user;
    private UploadedFile uploadedFile;
    private UUID uploadedFileId;

    @BeforeEach
    void setUp() {

        uploadedFileRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("test.integration@example.com")
                .username("TestUserForAnalysis")
                .password(passwordEncoder.encode("password123"))
                .createdAt(LocalDate.now())
                .build());


        uploadedFile = UploadedFile.builder()
                .user(user)
                .filename("analysis_integration_test.txt")
                .uploadTime(new Timestamp(System.currentTimeMillis()))
                .contentType(ContentType.TXT)
                .build();

        FileContent fileContent = FileContent.builder()
                .rawText("""
                Hello world! Hello again. This is a test, Egor.
                Stop words like 'this', 'is', 'a' should be excluded.
                Punctuation: ! . , ' - Egor Egor Egor.
                Testing numbers: 123, 456, 789.
                Special characters: @#$%^&*()
                """)
                .build();

        fileContent.setUploadedFile(uploadedFile);
        uploadedFile.setFileContent(fileContent);

        uploadedFile = uploadedFileRepository.save(uploadedFile);
        uploadedFileId = uploadedFile.getId();
        fileAnalysisService.setStopWordsRaw("this,is,a,like,should,be");
    }

    @Test
    void createAnalysis_shouldReturnCorrectDataSet() {

        int topN = 5;
        boolean excludeStopWords = true;

        FileAnalysisReadDto dto = fileAnalysisService.createAnalysis(uploadedFileId, topN, excludeStopWords);

        assertNotNull(dto);
        assertNotNull(dto.id());
        assertTrue(dto.stopWordsExcluded());

        // Проверка: топ-слова (Egor встречается 4 раза)
        Map<String, Long> topWords = dto.topWords();
        assertNotNull(topWords);
        assertTrue(topWords.containsKey("egor"), "Should contain 'egor'");
        assertEquals(4L, topWords.get("egor"), "'egor' should appear 4 times");

        // Проверка: стоп-слова исключены
        assertFalse(topWords.containsKey("this"), "'this' should be excluded");
        assertFalse(topWords.containsKey("is"), "'is' should be excluded");
        assertFalse(topWords.containsKey("a"), "'a' should be excluded");

        // Проверка: пунктуация
        Map<Character, Long> punctuation = dto.punctuationMap();
        assertTrue(punctuation.containsKey('.'), "Should contain '.'");
        assertTrue(punctuation.containsKey('!'), "Should contain '!'");
        assertTrue(punctuation.containsKey(','), "Should contain ','");
        assertTrue(punctuation.containsKey('\''), "Should contain '''");
        assertTrue(punctuation.containsKey('-'), "Should contain '-'");

        // Проверка точных значений пунктуации
        assertEquals(6L, punctuation.get('.'), "Should have 3 dots");
        assertEquals(2L, punctuation.get('!'), "Should have 2 exclamation");

        // Проверка: начальные буквы
        Map<Character, Long> startsWith = dto.startsWithMap();
        assertTrue(startsWith.containsKey('h'), "Should contain words starting with 'h'");
        assertEquals(2L, startsWith.get('h'), "Should have 2 words starting with 'h' (hello, hello)");
        assertTrue(startsWith.containsKey('e'), "Should contain words starting with 'e' (egor)");

        // Проверка: длина слов
        Map<String, Integer> wordLengths = dto.wordLengthMap();
        assertEquals(5, wordLengths.get("hello"), "'hello' should have length 5");
        assertEquals(4, wordLengths.get("egor"), "'egor' should have length 4");
        assertEquals(5, wordLengths.get("world"), "'world' should have length 5");
        assertEquals(4, wordLengths.get("test"), "'test' should have length 4");

        // Проверка: анализ сохранился в БД
        FileAnalysis savedAnalysis = fileAnalysisRepository.findByUploadedFile_Id(uploadedFileId)
                .orElseThrow(() -> new AssertionError("Analysis should be saved in database"));
        assertNotNull(savedAnalysis);
        assertEquals(dto.id(), savedAnalysis.getId());
        assertEquals(excludeStopWords, savedAnalysis.getStopWordsExcluded());
    }

    @Test
    void createAnalysis_shouldHandleEmptyStopWords() {

        fileAnalysisService.setStopWordsRaw("");
        int topN = 5;
        boolean excludeStopWords = true;

        FileAnalysisReadDto dto = fileAnalysisService.createAnalysis(uploadedFileId, topN, excludeStopWords);


        assertNotNull(dto);
        // Все слова должны быть включены, так как список стоп-слов пустой
        assertTrue(dto.topWords().containsKey("this"));
        assertTrue(dto.topWords().containsKey("is"));
        assertTrue(dto.topWords().containsKey("a"));
        assertTrue(dto.stopWordsExcluded()); // Флаг все равно true
    }

    @Test
    void createAnalysis_shouldWorkWithoutStopWordsExclusion() {

        int topN = 5;
        boolean excludeStopWords = false;

        FileAnalysisReadDto dto = fileAnalysisService.createAnalysis(uploadedFileId, topN, excludeStopWords);

        assertNotNull(dto);
        assertFalse(dto.stopWordsExcluded());
        // Стоп-слова должны присутствовать
        assertTrue(dto.topWords().containsKey("this"));
        assertTrue(dto.topWords().containsKey("is"));
        assertTrue(dto.topWords().containsKey("a"));
    }

    @Test
    void createAnalysis_shouldRespectTopNParameter() {

        int topN = 2; // Только 2 самых частых слова
        boolean excludeStopWords = true;

        FileAnalysisReadDto dto = fileAnalysisService.createAnalysis(uploadedFileId, topN, excludeStopWords);

        assertEquals(topN, dto.topWords().size(), "Should return exactly topN words");
        // 'egor' (4 раза) и 'hello' (2 раза) должны быть топ-2
        assertTrue(dto.topWords().containsKey("egor"));
        assertTrue(dto.topWords().containsKey("hello"));
        assertEquals(2, dto.topWords().size()); // Только 2 слова
    }

    @Test
    void createAnalysis_shouldThrowWhenFileNotFound() {

        UUID nonExistentFileId = UUID.randomUUID();
        int topN = 5;
        boolean excludeStopWords = true;

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> fileAnalysisService.createAnalysis(nonExistentFileId, topN, excludeStopWords));
    }

    @Test
    void createAnalysis_shouldThrowWhenFileHasNoContent() {
        UploadedFile emptyFile = UploadedFile.builder()

                .user(user)
                .filename("empty.txt")
                .uploadTime(new Timestamp(System.currentTimeMillis()))
                .contentType(ContentType.TXT)
                .build();
        emptyFile = uploadedFileRepository.save(emptyFile);

        UploadedFile finalEmptyFile = emptyFile;
        assertThrows(IllegalStateException.class,
                () -> fileAnalysisService.createAnalysis(finalEmptyFile.getId(), 5, true));
    }

    @Test
    void createAnalysis_shouldThrowWhenTextIsEmpty() {
        UploadedFile emptyTextFile = UploadedFile.builder()
                .user(user)
                .filename("empty_text.txt")
                .uploadTime(new Timestamp(System.currentTimeMillis()))
                .contentType(ContentType.TXT)
                .build();

        FileContent emptyContent = FileContent.builder()
                .rawText("   ")
                .build();
        emptyContent.setUploadedFile(emptyTextFile);
        emptyTextFile.setFileContent(emptyContent);

        emptyTextFile = uploadedFileRepository.save(emptyTextFile);

        UploadedFile finalEmptyTextFile = emptyTextFile;
        assertThrows(IllegalStateException.class,
                () -> fileAnalysisService.createAnalysis(finalEmptyTextFile.getId(), 5, true));
    }

    @Test
    void getAnalysisByFileId_shouldReturnAnalysisAfterCreation() {

        FileAnalysisReadDto createdDto = fileAnalysisService.createAnalysis(uploadedFileId, 5, true);
        assertNotNull(createdDto);

        var optionalDto = fileAnalysisService.getAnalysisByFileId(uploadedFileId);

        assertTrue(optionalDto.isPresent());
        FileAnalysisReadDto retrievedDto = optionalDto.get();

        assertEquals(createdDto.id(), retrievedDto.id());
        assertEquals(createdDto.topWords(), retrievedDto.topWords());
        assertEquals(createdDto.stopWordsExcluded(), retrievedDto.stopWordsExcluded());
    }

    @Test
    void getAnalysisByFileId_shouldReturnEmptyWhenNoAnalysis() {

        var optionalDto = fileAnalysisService.getAnalysisByFileId(uploadedFileId);


        assertTrue(optionalDto.isEmpty());
    }

    @Test
    void createAnalysis_shouldThrowWhenAnalysisAlreadyExists() {

        fileAnalysisService.createAnalysis(uploadedFileId, 5, true);

        assertThrows(IllegalStateException.class,
                () -> fileAnalysisService.createAnalysis(uploadedFileId, 5, true));
    }
}