package by.egrius.app.unit.service;

import by.egrius.app.dto.fileDTO.FileContentReadDto;
import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.FileContent;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.mapper.fileMapper.UploadedFileReadMapper;
import by.egrius.app.publisher.FileEventPublisher;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UploadedFileService;
import by.egrius.app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private  UserRepository userRepository;

    @Mock
    private  PasswordEncoder passwordEncoder;

    @Mock
    private  UploadedFileRepository uploadedFileRepository;

    @Mock
    private  UploadedFileReadMapper uploadedFileReadMapper;

    @Mock
    private FileEventPublisher fileEventPublisher;

    @InjectMocks
    private UploadedFileService fileService;

    @Test
    void uploadFile_shouldReturnDtoOfCreatedFile() {
        UUID fileId = UUID.randomUUID();
        User user = User.builder()
                .userId(UUID.randomUUID())
                .username("TestName")
                .password("hashed_pwd")
                .createdAt(LocalDate.now())
                .build();

        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "TestFileName.txt",
                "text/plain",
                "Hello world\nThis is a test".getBytes(StandardCharsets.UTF_8)
        );

        UploadedFile uploadedFile = UploadedFile.builder()
                .id(fileId)
                .user(user)
                .filename("TestFileName")
                .uploadTime(Timestamp.valueOf(LocalDateTime.now()))
                .contentType(ContentType.TXT)
                .build();

        UploadedFileReadDto expectedDto = new UploadedFileReadDto(
                uploadedFile.getId(),
                uploadedFile.getFilename(),
                uploadedFile.getUploadTime(),
                uploadedFile.getContentType()
        );
        when(userRepository.findById(user.getUserId()))
                .thenReturn(Optional.of(user));
        when(uploadedFileReadMapper.map(any(UploadedFile.class))).thenReturn(expectedDto);
        when(uploadedFileRepository.save(any(UploadedFile.class)))
                .thenAnswer(invocation -> {
                    UploadedFile f = invocation.getArgument(0);
                    f.setId(fileId);
                    return f;
                });

        UploadedFileReadDto result = fileService.uploadFile(mockFile, user.getUserId());

        assertEquals(fileId, result.id());

        verify(userRepository).findById(user.getUserId());
        verify(uploadedFileRepository).save(any(UploadedFile.class));
        verify(uploadedFileReadMapper).map(any(UploadedFile.class));
        verify(fileEventPublisher).publishUpload(fileId);
    }

    @Test
    void uploadFile_shouldThrowIllegalArgumentException_whenItsEmpty() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "TestFileName.txt",
                "text/plain",
                "".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(IllegalArgumentException.class, () -> fileService.uploadFile(mockFile, UUID.randomUUID()));
    }

    @Test
    void uploadFile_shouldThrowIllegalArgumentException_whenFileSizeExceedsLimit() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .userId(userId)
                .username("TestName")
                .password("hashed_pwd")
                .createdAt(LocalDate.now())
                .build();

        // Создаем файл больше 10MB
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
        Arrays.fill(largeContent, (byte) 'A');

        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "TestFileName.txt",
                "text/plain",
                largeContent
        );

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadFile(mockFile, userId));

        verify(uploadedFileRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void uploadFile_shouldThrowIllegalArgumentException_whenFilenameAlreadyExists() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .userId(userId)
                .username("TestName")
                .password("hashed_pwd")
                .createdAt(LocalDate.now())
                .build();

        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "existing_file.txt",
                "text/plain",
                "Hello world".getBytes(StandardCharsets.UTF_8)
        );

        UploadedFile existingFile = UploadedFile.builder()
                .id(UUID.randomUUID())
                .filename("existing_file.txt")
                .user(user)
                .build();

        when(uploadedFileRepository.findByFilenameAndUserId("existing_file.txt", userId))
                .thenReturn(Optional.of(existingFile));

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadFile(mockFile, userId));

        verify(uploadedFileRepository, never()).save(any());
    }

    @Test
    void showUploadedFileById() {
        UUID expectedFileUUID = UUID.randomUUID();
        UUID expectedUserUUID = UUID.randomUUID();

        User expectedUser = User.builder()
                .userId(expectedUserUUID)
                .username("TestName")
                .password("hashed_pwd")
                .createdAt(LocalDate.now())
                .build();

        UploadedFile uploadedFile = UploadedFile.builder()
                .user(expectedUser)
                .id(expectedFileUUID)
                .filename("TestFileName")
                .uploadTime(Timestamp.valueOf(LocalDateTime.now()))
                .contentType(ContentType.TXT)
                .build();

        UploadedFileReadDto expectedFileDto = new UploadedFileReadDto(

                uploadedFile.getId(),
                uploadedFile.getFilename(),
                uploadedFile.getUploadTime(),
                uploadedFile.getContentType()
        );

        when(uploadedFileRepository.findByFileIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(uploadedFile));
        when(uploadedFileReadMapper.map(any(UploadedFile.class))).thenReturn(expectedFileDto);

        UploadedFileReadDto actualResult = fileService.showUploadedFileById(expectedFileUUID, expectedUserUUID);

        assertNotNull(actualResult);
        assertEquals(actualResult.id(), expectedFileDto.id());
    }

    @Test
    void showUploadedFileById_shouldThrowEntityNotFoundException_whenFileNotFound() {
        UUID wrongUserId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        when(uploadedFileRepository.findByFileIdAndUserId(fileId, wrongUserId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> fileService.showUploadedFileById(wrongUserId, fileId));
    }

    @Test
    void showAllUploadedFilesByUserId() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        User user = User.builder().userId(userId).username("TestUser").build();

        UploadedFile file = UploadedFile.builder()
                .id(UUID.randomUUID())
                .filename("file.txt")
                .user(user)
                .build();

        UploadedFileReadDto dto = new UploadedFileReadDto(
                file.getId(), file.getFilename(), file.getUploadTime(), file.getContentType()
        );

        Page<UploadedFile> page = new PageImpl<>(List.of(file), pageable, 1);

        when(uploadedFileRepository.findAllFilesByUserId(userId, pageable)).thenReturn(page);
        when(uploadedFileReadMapper.map(file)).thenReturn(dto);

        Page<UploadedFileReadDto> result = fileService.showAllUploadedFilesByUserId(userId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto.id(), result.getContent().getFirst().id());
    }

    @Test
    void getFileContent_shouldReturnContentIfCorrect() {
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        User user = User.builder().userId(userId).username("TestUser").build();

        UploadedFile file = UploadedFile.builder()
                .id(fileId)
                .filename("file.txt")
                .user(user)
                .build();

        FileContent content = FileContent.builder()
                .id(fileId)
                .rawText("Test text")
                .lineCount(1L)
                .wordCount(2L)
                .uploadedFile(file)
                .build();

        file.setFileContent(content);

        when(uploadedFileRepository.findByIdWithUserAndContent(fileId, userId))
                .thenReturn(Optional.of(file));

        FileContentReadDto result = fileService.getFileContent(userId, fileId);

        assertNotNull(result);
        assertEquals("Test text", result.rawText());
        assertEquals(1L, result.lineCount());
        assertEquals(2L, result.wordCount());
        assertNull(result.language());
    }

    @Test
    void getFileContent_shouldThrowEntityNotFoundException_whenFileNotFound() {
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        when(uploadedFileRepository.findByIdWithUserAndContent(fileId, userId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> fileService.getFileContent(userId, fileId));
    }

    @Test
    void getFileContent_shouldThrowAccessDeniedException_whenFileBelongsToAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User anotherUser = User.builder().userId(anotherUserId).username("OtherUser").build();
        UploadedFile file = UploadedFile.builder()
                .id(fileId)
                .filename("file.txt")
                .user(anotherUser)
                .build();

        when(uploadedFileRepository.findByIdWithUserAndContent(fileId, userId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> fileService.getFileContent(userId, fileId));
    }

    @Test
    void getFileContent_shouldThrowEntityNotFoundException_whenFileContentIsNull() {
        UUID userId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User user = User.builder().userId(userId).username("TestUser").build();

        UploadedFile file = UploadedFile.builder()
                .id(fileId)
                .filename("file.txt")
                .user(user)
                .build();

        when(uploadedFileRepository.findByIdWithUserAndContent(fileId, userId))
                .thenReturn(Optional.of(file));

        assertThrows(EntityNotFoundException.class,
                () -> fileService.getFileContent(userId, fileId));
    }

    @Test
    void uploadFile_shouldThrowIllegalStateException_whenIOExceptionOccurs() throws IOException {
        UUID userId = UUID.randomUUID();
        MultipartFile mockFile = mock(MultipartFile.class);

        when(mockFile.getOriginalFilename()).thenReturn("bad.txt");
        when(mockFile.getBytes()).thenThrow(new IOException("Simulated IO error"));

        assertThrows(IllegalStateException.class,
                () -> fileService.uploadFile(mockFile, userId));
    }

    @Test
    void removeFileById_shouldDeleteFileIfExists() throws AccessDeniedException {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawPassword = "1234";

        User currentUser = User.builder()
                .userId(userId)
                .password("hashed_password")
                .username("TestUser")
                .email("testEmail@gmail.com")
                .build();

        UploadedFile file = UploadedFile.builder()
                .id(fileId)
                .user(currentUser)
                .filename("file.txt")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(uploadedFileRepository.findByFileIdAndUserId(fileId, userId))
                .thenReturn(Optional.of(file));
        when(passwordEncoder.matches(rawPassword, currentUser.getPassword())).thenReturn(true);

        assertDoesNotThrow(() -> fileService.removeFileById(userId, rawPassword, fileId));

        verify(userService).getCurrentUser();
        verify(uploadedFileRepository).delete(file);
        verify(fileEventPublisher).publishDeleted(fileId);
    }

    @Test
    void removeFileById_shouldThrowAccessDeniedException_whenPasswordIsWrong() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawPassword = "wrong_pwd";

        User currentUser = User.builder()
                .userId(userId)
                .password("hashed_password")
                .username("TestUser")
                .build();

        UploadedFile file = UploadedFile.builder()
                .id(fileId)
                .user(currentUser)
                .filename("file.txt")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(uploadedFileRepository.findByFileIdAndUserId(fileId, userId))
                .thenReturn(Optional.of(file));
        when(passwordEncoder.matches(rawPassword, currentUser.getPassword())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> fileService.removeFileById(userId, rawPassword, fileId));

        verify(uploadedFileRepository, never()).delete(any());
        verify(fileEventPublisher, never()).publishDeleted(any());
    }

    @Test
    void removeFileById_shouldThrowEntityNotFoundException_whenFileNotFound() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawPassword = "1234";

        User currentUser = User.builder()
                .userId(userId)
                .password("hashed")
                .username("test")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);

        when(uploadedFileRepository.findByFileIdAndUserId(fileId, userId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> fileService.removeFileById(userId, rawPassword, fileId));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(uploadedFileRepository, never()).delete(any());
        verify(fileEventPublisher, never()).publishDeleted(any());
    }

    @Test
    void removeFileByFilename_shouldDeleteFileIfExists() throws AccessDeniedException {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawPassword = "1234";
        String filename = "file.txt";

        User currentUser = User.builder()
                .userId(userId)
                .password("hashed_password")
                .username("TestUser")
                .build();

        UploadedFile file = UploadedFile.builder()
                .id(fileId)
                .user(currentUser)
                .filename(filename)
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(uploadedFileRepository.findByFilenameAndUserId(filename, userId))
                .thenReturn(Optional.of(file));
        when(passwordEncoder.matches(rawPassword, currentUser.getPassword())).thenReturn(true);

        fileService.removeFileByFilename(userId, rawPassword, filename);

        verify(userService).getCurrentUser();
        verify(uploadedFileRepository).delete(file);
        verify(fileEventPublisher).publishDeleted(fileId);
    }

    @Test
    void removeFileById_shouldNotDeleteIfFileNotFound() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawPassword = "1234";

        User currentUser = User.builder()
                .userId(userId)
                .password("hashed")
                .username("test")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);

        when(uploadedFileRepository.findByFileIdAndUserId(fileId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.removeFileById(userId, rawPassword,fileId));
        verify(passwordEncoder, never()).matches(any(), any());
        verify(uploadedFileRepository, never()).deleteById(any());
    }

    @Test
    void removeFileById_shouldThrowAccessDeniedException_whenUserIdsDontMatch() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        String rawPassword = "1234";

        User currentUser = User.builder()
                .userId(currentUserId)
                .password("hashed_password")
                .username("TestUser")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);

        assertThrows(AccessDeniedException.class,
                () -> fileService.removeFileById(userId, rawPassword, fileId));

        verify(uploadedFileRepository, never()).findByFileIdAndUserId(any(), any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void removeFileByFilename_shouldThrowAccessDeniedException_whenPasswordIsWrong() {
        UUID userId = UUID.randomUUID();
        String rawPassword = "wrong_pwd";
        String filename = "file.txt";

        User user = User.builder()
                .userId(userId)
                .password("hashed_password")
                .username("TestUser")
                .build();

        UploadedFile file = UploadedFile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .filename(filename)
                .build();


        when(userService.getCurrentUser()).thenReturn(user);

        when(uploadedFileRepository.findByFilenameAndUserId(filename, userId))
                .thenReturn(Optional.of(file));
        when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> fileService.removeFileByFilename(userId, rawPassword, filename));

        verify(uploadedFileRepository, never()).delete(any());
        verify(fileEventPublisher, never()).publishDeleted(any());
    }

    @Test
    void removeFileByFilename_shouldThrowEntityNotFoundException_whenFileNotFound() {
        UUID userId = UUID.randomUUID();
        String rawPassword = "1234";
        String filename = "file.txt";

        User currentUser = User.builder()
                .userId(userId)
                .password("hashed")
                .username("test")
                .build();

        when(userService.getCurrentUser()).thenReturn(currentUser);

        when(uploadedFileRepository.findByFilenameAndUserId(filename, userId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> fileService.removeFileByFilename(userId, rawPassword, filename));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(uploadedFileRepository, never()).delete(any());
        verify(fileEventPublisher, never()).publishDeleted(any());
    }
}