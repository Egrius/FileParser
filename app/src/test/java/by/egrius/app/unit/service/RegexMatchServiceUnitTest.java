package by.egrius.app.unit.service;

import by.egrius.app.dto.fileDTO.RegexMatchReadDto;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.entity.enums.PatternType;
import by.egrius.app.mapper.RegexMatchReadMapper;
import by.egrius.app.repository.RegexMatchRepository;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.service.RegexMatchService;
import by.egrius.app.service.UploadedFileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RegexMatchServiceUnitTest {

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private RegexMatchRepository regexMatchRepository;

    @Mock
    private RegexMatchReadMapper regexMatchReadMapper;

    @Mock
    private UploadedFileService uploadedFileService;

    @InjectMocks
    private RegexMatchService regexMatchService;

    @Test
    void createRegexMatch_shouldReturnCorrectResult() {

        UUID mockFileId = UUID.randomUUID();

        String rawMockText = """
            Иван Иванов зарегистрировался 12.03.2023, email: ivan.ivanov@example.com,
            телефон: +375 (29) 123-45-67, IP: 192.168.0.1.
            Мария Петрова: 01.01.2022, maria_pet@domain.by, +375 33 9876543, IP: 10.0.0.254.
            Некорректные: 99.99.9999, user@@domain..com, +375123456789, 999.999.999.999.
            Дополнительно: support@company.org, +375 25 555-55-55, 172.16.254.1, 05.11.2025.
            """;

        FileContent mockFileContent = FileContent.builder()
                .rawText(rawMockText)
                .build();

        UploadedFile mockFile = UploadedFile.builder()
                .id(mockFileId)
                .filename("testFile.txt")
                .uploadTime(Timestamp.valueOf(LocalDateTime.now()))
                .contentType(ContentType.TXT)
                .fileContent(mockFileContent)
                .build();


        List<String> expectedEmails = List.of("ivan.ivanov@example.com", "maria_pet@domain.by", "support@company.org");
        List<String> expectedPhones = List.of("+375 (29) 123-45-67", "+375 33 9876543", "+375 25 555-55-55");
        List<String> expectedIps = List.of("192.168.0.1", "10.0.0.254", "172.16.254.1");
        List<String> expectedDates = List.of("12.03.2023", "01.01.2022", "05.11.2025");


        RegexMatchReadDto expectedDto = new RegexMatchReadDto(
                expectedEmails,
                expectedPhones,
                expectedIps,
                expectedDates,
                (long)(expectedEmails.size() + expectedPhones.size() + expectedIps.size() + expectedDates.size())
        );

        when(uploadedFileRepository.findById(mockFileId)).thenReturn(Optional.of(mockFile));
        when(regexMatchReadMapper.map(any())).thenReturn(expectedDto);

        RegexMatchReadDto actual = regexMatchService.createRegexMatch(
                mockFileId,
                Set.of(PatternType.EMAIL, PatternType.PHONE, PatternType.IP, PatternType.DATE)
        );

        assertEquals(expectedDto.matchCount(), actual.matchCount());
        assertTrue(actual.emailMatches().containsAll(expectedEmails));
        assertTrue(actual.phoneMatches().containsAll(expectedPhones));
        assertTrue(actual.ipMatches().containsAll(expectedIps));
        assertTrue(actual.dateMatches().containsAll(expectedDates));

        System.out.println("Match count: " + actual.matchCount());
        System.out.println("Emails: " + actual.emailMatches());
        System.out.println("Phones:  " + actual.phoneMatches());
        System.out.println("Ips:  " + actual.ipMatches());
        System.out.println("Dates:  " + actual.dateMatches());
    }
}