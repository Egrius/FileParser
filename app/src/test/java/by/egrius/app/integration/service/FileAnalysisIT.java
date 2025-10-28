package by.egrius.app.integration.service;

import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.repository.FileAnalysisRepository;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.FileAnalysisService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    FileAnalysisService fileAnalysisService;

    private UploadedFile uploadedFile;

    private UUID uploadedId;

    @BeforeAll
    void setup() {

        User user = User.builder()
                .userId(UUID.randomUUID())
                .email("emailForFun_3@gmail.com")
                .username("TestUserForAnalysis_1")
                .password(passwordEncoder.encode("1234"))
                .createdAt(LocalDate.now())
                .build();

        user = userRepository.save(user);

        String rawText = """
            Hello world! Hello again. This is a test, Egor.
            Stop words like 'this', 'is', 'a' should be excluded.
            Punctuation: ! . , ' - Egor Egor Egor.
            """;

        uploadedId = UUID.randomUUID();

        uploadedFile = UploadedFile.builder()
                .id(uploadedId)
                .user(user)
                .filename("analysis_test.txt")
                .uploadTime(new Timestamp(System.currentTimeMillis()))
                .contentType(ContentType.TXT)
                .build();

        FileContent fileContent = FileContent.builder()
                .rawText(rawText)
                .build();

        fileContent.setUploadedFile(uploadedFile);

        uploadedFile.setFileContent(fileContent);

        uploadedFile = uploadedFileRepository.save(uploadedFile);

        fileAnalysisService.setStopWordsRaw("this,is,a");
    }

    @Test
    void createAnalysis_shouldReturnCorrectDataSet() {
            int topN = 3;
            boolean excludeStopWords = true;

            var dto = fileAnalysisService.createAnalysis(uploadedId, topN, excludeStopWords);

            // Проверка: топ-слова
            assertEquals(topN, dto.topWords().size());
            assertTrue(dto.topWords().containsKey("egor"));
            assertEquals(4L, dto.topWords().get("egor"));

            // Проверка: символы пунктуации
            assertTrue(dto.punctuationMap().containsKey('.'));
            assertTrue(dto.punctuationMap().get('.') >= 2);

            // Проверка: карта длин слов
            assertEquals("hello".length(), dto.wordLengthMap().get("hello"));

            // Проверка: начальные буквы
            assertTrue(dto.startsWithMap().containsKey('h'));
            assertTrue(dto.startsWithMap().get('h') >= 2);

            // Проверка: флаг стоп-слов
            assertTrue(dto.stopWordsExcluded());
    }
}