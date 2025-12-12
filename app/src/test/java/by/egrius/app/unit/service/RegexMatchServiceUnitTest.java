package by.egrius.app.unit.service;

import by.egrius.app.dto.fileDTO.RegexMatchReadDto;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.PatternMatches;
import by.egrius.app.entity.RegexMatch;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.entity.enums.PatternType;
import by.egrius.app.mapper.RegexMatchReadMapper;
import by.egrius.app.repository.PatternMatchesRepository;
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
    private RegexMatchReadMapper regexMatchReadMapper;

    @Mock
    RegexMatchRepository regexMatchRepository;

    @Mock
    PatternMatchesRepository patternMatchesRepository;

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
        when(regexMatchRepository.findByUploadedFileId(mockFileId)).thenReturn(Optional.empty());
        when(regexMatchRepository.save(any(RegexMatch.class))).thenAnswer(invocation -> {
            RegexMatch savedRegexMatch = invocation.getArgument(0);
            savedRegexMatch.setId(UUID.randomUUID());
            return savedRegexMatch;
        });
        when(regexMatchReadMapper.map(any(RegexMatch.class))).thenReturn(expectedDto);


        RegexMatchReadDto actual = regexMatchService.createRegexMatch(
                mockFileId,
                Set.of(PatternType.EMAIL, PatternType.PHONE, PatternType.IP, PatternType.DATE)
        );

        assertNotNull(actual);
        assertEquals(expectedDto.matchCount(), actual.matchCount());
        assertEquals(expectedEmails, actual.emailMatches());
        assertEquals(expectedPhones, actual.phoneMatches());
        assertEquals(expectedIps, actual.ipMatches());
        assertEquals(expectedDates, actual.dateMatches());

        verify(uploadedFileRepository).findById(mockFileId);
        verify(regexMatchRepository).findByUploadedFileId(mockFileId);
        verify(regexMatchRepository).save(any(RegexMatch.class));
        verify(regexMatchReadMapper).map(any(RegexMatch.class));

        System.out.println("Match count: " + actual.matchCount());
        System.out.println("Emails: " + actual.emailMatches());
        System.out.println("Phones:  " + actual.phoneMatches());
        System.out.println("Ips:  " + actual.ipMatches());
        System.out.println("Dates:  " + actual.dateMatches());
    }

    @Test
    void createRegexMatch_shouldDeleteExistingMatch() {
        // Arrange
        UUID mockFileId = UUID.randomUUID();
        UploadedFile mockFile = UploadedFile.builder()
                .id(mockFileId)
                .fileContent(FileContent.builder().rawText("test").build())
                .build();

        RegexMatch existingMatch = new RegexMatch();
        existingMatch.setId(UUID.randomUUID());

        when(uploadedFileRepository.findById(mockFileId)).thenReturn(Optional.of(mockFile));
        when(regexMatchRepository.findByUploadedFileId(mockFileId)).thenReturn(Optional.of(existingMatch));
        when(regexMatchRepository.save(any(RegexMatch.class))).thenReturn(new RegexMatch());
        when(regexMatchReadMapper.map(any(RegexMatch.class))).thenReturn(new RegexMatchReadDto(
                List.of(), List.of(), List.of(), List.of(), 0L
        ));

        regexMatchService.createRegexMatch(mockFileId, Set.of(PatternType.EMAIL));

        verify(regexMatchRepository).delete(existingMatch);
        verify(regexMatchRepository, times(2)).flush();
    }

    @Test
    void createRegexMatch_shouldThrowWhenFileNotFound() {

        UUID nonExistentFileId = UUID.randomUUID();
        when(uploadedFileRepository.findById(nonExistentFileId)).thenReturn(Optional.empty());


        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            regexMatchService.createRegexMatch(nonExistentFileId, Set.of(PatternType.EMAIL));
        });
    }

    @Test
    void createRegexMatch_shouldThrowWhenNoTextContent() {

        UUID mockFileId = UUID.randomUUID();
        UploadedFile mockFile = UploadedFile.builder()
                .id(mockFileId)
                .fileContent(FileContent.builder().rawText(null).build())
                .build();

        when(uploadedFileRepository.findById(mockFileId)).thenReturn(Optional.of(mockFile));

        assertThrows(IllegalArgumentException.class, () -> {
            regexMatchService.createRegexMatch(mockFileId, Set.of(PatternType.EMAIL));
        });
    }

    @Test
    void getRegexMatchByFileId_shouldReturnDto() {

        UUID fileId = UUID.randomUUID();
        RegexMatch regexMatch = new RegexMatch();
        regexMatch.setId(UUID.randomUUID());

        RegexMatchReadDto expectedDto = new RegexMatchReadDto(
                List.of("test@example.com"),
                List.of(),
                List.of(),
                List.of(),
                1L
        );

        when(regexMatchRepository.findByUploadedFileId(fileId)).thenReturn(Optional.of(regexMatch));
        when(regexMatchReadMapper.map(regexMatch)).thenReturn(expectedDto);

        Optional<RegexMatchReadDto> result = regexMatchService.getRegexMatchByFileId(fileId);

        assertTrue(result.isPresent());
        assertEquals(expectedDto, result.get());
        verify(regexMatchRepository).findByUploadedFileId(fileId);
    }

    @Test
    void getRegexMatchByFileId_shouldReturnEmptyWhenNotFound() {

        UUID fileId = UUID.randomUUID();
        when(regexMatchRepository.findByUploadedFileId(fileId)).thenReturn(Optional.empty());

        Optional<RegexMatchReadDto> result = regexMatchService.getRegexMatchByFileId(fileId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPatternMatchesByType_shouldReturnMatches() {

        UUID fileId = UUID.randomUUID();
        PatternType type = PatternType.EMAIL;

        PatternMatches match1 = PatternMatches.builder()
                .matchId(UUID.randomUUID())
                .patternType(PatternType.EMAIL)
                .match("test1@example.com")
                .build();

        PatternMatches match2 = PatternMatches.builder()
                .matchId(UUID.randomUUID())
                .patternType(PatternType.EMAIL)
                .match("test2@example.com")
                .build();

        List<PatternMatches> expectedMatches = List.of(match1, match2);

        when(patternMatchesRepository.findByRegexMatchUploadedFileIdAndPatternType(fileId, type))
                .thenReturn(expectedMatches);

        List<PatternMatches> result = regexMatchService.getPatternMatchesByType(fileId, type);

        assertEquals(2, result.size());
        verify(patternMatchesRepository).findByRegexMatchUploadedFileIdAndPatternType(fileId, type);
    }

    @Test
    void deleteRegexMatch_shouldDeleteWhenExists() {

        UUID fileId = UUID.randomUUID();
        RegexMatch regexMatch = new RegexMatch();
        regexMatch.setId(UUID.randomUUID());

        when(regexMatchRepository.findByUploadedFileId(fileId)).thenReturn(Optional.of(regexMatch));

        regexMatchService.deleteRegexMatch(fileId);

        verify(regexMatchRepository).delete(regexMatch);
    }

    @Test
    void deleteRegexMatch_shouldDoNothingWhenNotFound() {

        UUID fileId = UUID.randomUUID();
        when(regexMatchRepository.findByUploadedFileId(fileId)).thenReturn(Optional.empty());

        regexMatchService.deleteRegexMatch(fileId);

        verify(regexMatchRepository, never()).delete(any());
    }
}