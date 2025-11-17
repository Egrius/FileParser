package by.egrius.app.unit.service;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.mapper.fileMapper.UploadedFileReadMapper;
import by.egrius.app.publisher.FileEventPublisher;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UploadedFileService;
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

import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Mock
    private FileEventPublisher fileEventPublisher;

    @InjectMocks
    private UploadedFileService fileService;

    @Test
    void uploadFile_shouldThrowException_whenFileIsEmpty() {
        UUID userId = UUID.randomUUID();
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadFile(emptyFile, userId));
    }

    @Test
    void uploadFile_shouldThrowException_whenFileIsNull() {
        UUID userId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadFile(null, userId));
    }

    @Test
    void uploadFile_shouldReturnDtoOfCreatedFile() {
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
                .user(user)
                .id(UUID.randomUUID())
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

        when(uploadedFileReadMapper.map(any(UploadedFile.class))).thenReturn(expectedDto);
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenReturn(uploadedFile);

        UploadedFileReadDto result = fileService.uploadFile(mockFile, user.getUserId());

        assertEquals(expectedDto.id(), result.id());
        assertEquals(expectedDto.filename(), result.filename());

        verify(uploadedFileRepository).save(any(UploadedFile.class));
        verify(fileEventPublisher).publishUpload(any(UUID.class));
        verify(uploadedFileReadMapper).map(any(UploadedFile.class));
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

        when(uploadedFileRepository.findByIdAndUser_UserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(uploadedFile));
        when(uploadedFileReadMapper.map(any(UploadedFile.class))).thenReturn(expectedFileDto);

        UploadedFileReadDto actualResult = fileService.showUploadedFileById(expectedFileUUID, expectedUserUUID);

        assertNotNull(actualResult);
        assertEquals(actualResult.id(), expectedFileDto.id());
    }

    @Test
    void showUploadedFileById_shouldThrowException_whenFileNotFound() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(uploadedFileRepository.findByIdAndUser_UserId(fileId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> fileService.showUploadedFileById(fileId, userId));
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
    void removeFile_shouldDeleteFileIfExists() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawPassword = "1234";

        User user = User.builder()
                .userId(userId)
                .password("hashed_password")
                .username("TestUser")
                .build();

        UploadedFile file = UploadedFile.builder()
                .id(fileId)
                .user(user)
                .filename("file.txt")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(true);


        try {
            fileService.removeFile(userId, rawPassword, fileId);
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }

        verify(uploadedFileRepository).delete(file);
        verify(fileEventPublisher).publishDeleted(fileId);

    }

    @Test
    void removeFile_shouldThrowException_whenPasswordIsWrong() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder().userId(userId).password("hashed_pwd").build();
        UploadedFile file = UploadedFile.builder().id(fileId).user(user).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> fileService.removeFile(userId, "wrong", fileId));
    }

    @Test
    void removeFile_shouldThrowException_whenFileBelongsToAnotherUser() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        User user = User.builder().userId(userId).password("hashed_pwd").build();
        User anotherUser = User.builder().userId(anotherUserId).password("hashed_pwd").build();

        UploadedFile file = UploadedFile.builder().id(fileId).user(anotherUser).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(passwordEncoder.matches("1234", user.getPassword())).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> fileService.removeFile(userId, "1234", fileId));
    }

    @Test
    void removeFile_shouldNotDeleteIfFileNotFound() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawPassword = "1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(null, "test", "test@gmail.com", encodedPassword, LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(uploadedFileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> fileService.removeFile(userId, rawPassword, fileId));

        verify(uploadedFileRepository, never()).delete(any());
    }
}