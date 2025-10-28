package by.egrius.app.integration.service;

import by.egrius.app.dto.fileDTO.RegexMatchReadDto;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.entity.enums.PatternType;
import by.egrius.app.mapper.RegexMatchReadMapper;
import by.egrius.app.repository.RegexMatchRepository;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.RegexMatchService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegexMatchServiceIT {

    @Autowired
    private  UploadedFileRepository uploadedFileRepository;

    @Autowired
    private  RegexMatchRepository regexMatchRepository;

    @Autowired
    private  RegexMatchReadMapper regexMatchReadMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegexMatchService regexMatchService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private FileContent fileContent;

    private UploadedFile uploadedFile;

    private String rawText;

    private User user;

    private UUID uploadedFileId;

    @BeforeEach
    public void setup() {
        rawText = """
            Иван Иванов зарегистрировался 12.03.2023, email: ivan.ivanov@example.com,
            телефон: +375 (29) 123-45-67, IP: 192.168.0.1.
            Мария Петрова: 01.01.2022, maria_pet@domain.by, +375 33 9876543, IP: 10.0.0.254.
            Некорректные: 99.99.9999, user@@domain..com, +375123456789, 999.999.999.999.
            Дополнительно: support@company.org, +375 25 555-55-55, 172.16.254.1, 05.11.2025.
        """;

        user = User.builder()
                .userId(UUID.randomUUID())
                .username("RegexMatchUser")
                .email("regexMatch@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .createdAt(LocalDate.now())
                .build();

        uploadedFile = UploadedFile.builder()
                .id(UUID.randomUUID())         // вручную назначаем id или используем @GeneratedValue
                .user(user)
                .filename("testFile.txt")
                .uploadTime(Timestamp.valueOf(LocalDateTime.now()))
                .contentType(ContentType.TXT)
                .build();

        fileContent = FileContent.builder()
                .uploadedFile(uploadedFile)
                .rawText(rawText)
                .build();

        uploadedFile.setFileContent(fileContent);
        user.setFiles(List.of(uploadedFile));

        userRepository.save(user);
        userRepository.flush();

        uploadedFileId = uploadedFile.getId();

    }

    @Test
    void createRegexMatch() {
        Set<PatternType> patternTypes = Set.of(PatternType.EMAIL, PatternType.PHONE);

        RegexMatchReadDto actualDto = regexMatchService.createRegexMatch(uploadedFileId, patternTypes);

        List<String> expectedEmails = List.of("ivan.ivanov@example.com", "maria_pet@domain.by", "support@company.org");
        List<String> expectedPhones = List.of("+375 (29) 123-45-67", "+375 33 9876543", "+375 25 555-55-55");

        long expectedCount = expectedEmails.size() + expectedPhones.size();

        assertEquals(expectedCount, actualDto.matchCount());
        assertTrue(actualDto.emailMatches().containsAll(expectedEmails));
        assertTrue(actualDto.phoneMatches().containsAll(expectedPhones));

        System.out.println("Match count: " + actualDto.matchCount());
        System.out.println("Emails: " + actualDto.emailMatches());
        System.out.println("Phones: " + actualDto.phoneMatches());
        System.out.println("IPs: " + actualDto.ipMatches());
        System.out.println("Dates: " + actualDto.dateMatches());

        Optional<UploadedFile> savedFile = uploadedFileRepository.findById(uploadedFileId);
        assertTrue(savedFile.isPresent());
        assertEquals(user.getUserId(), savedFile.get().getUser().getUserId());
    }
}