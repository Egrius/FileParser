package by.egrius.app.unit.service;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.entity.enums.Language;
import by.egrius.app.mapper.fileMapper.UploadedFileReadMapper;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceUnitTest {

    @Mock
    private  UserRepository userRepository;

    @Mock
    private  PasswordEncoder passwordEncoder;

    @Mock
    private  UploadedFileRepository uploadedFileRepository;

    @Mock
    private  UploadedFileReadMapper uploadedFileReadMapper;

    @InjectMocks
    private FileService fileService;

    @Test
    void uploadFile_shouldReturnDtoOfCreatedFile() {
        User user = User.builder()
                .userId(UUID.randomUUID())
                .username("TestName")
                .password("hashed_pwd")
                .createdAt(LocalDate.now())
                .files(Collections.emptyList())
                .build();

        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "TestFileName.txt",
                "text/plain",
                "Hello world\nThis is a test".getBytes(StandardCharsets.UTF_8)
        );

        UploadedFile uploadedFile = UploadedFile.builder()
                .user(user)
                .id(UUID.randomUUID())
                .filename("TestFileName")
                .uploadTime(Timestamp.valueOf(LocalDateTime.now()))
                .contentType(ContentType.TXT)
                .build();

        FileContent fileContent = FileContent.builder()
                .id(uploadedFile.getId())
                .uploadedFile(uploadedFile)
                .rawText("")
                .lineCount(0L)
                .wordCount(0L)
                .language(Language.UNKNOWN)
                .build();

        UploadedFileReadDto expectedDto = new UploadedFileReadDto(
                new UserReadDto(user.getUserId(), user.getUsername(), user.getEmail(), user.getCreatedAt()),
                uploadedFile.getId(),
                uploadedFile.getFilename(),
                uploadedFile.getUploadTime(),
                uploadedFile.getContentType()
        );


        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(uploadedFileReadMapper.map(any(UploadedFile.class))).thenReturn(expectedDto);
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenReturn(uploadedFile);

        UploadedFileReadDto result = fileService.uploadFile(mockFile, user.getUserId());

        assertEquals(expectedDto.id(), result.id());
        assertEquals(expectedDto.filename(), result.filename());

        verify(uploadedFileRepository).save(any(UploadedFile.class));
        verify(uploadedFileReadMapper).map(any(UploadedFile.class));
    }

    @Test
    void showUploadedFileById() {
    }

    @Test
    void showAllUploadedFilesByUserId() {
    }

    @Test
    void removeFile() {
    }
}