package by.egrius.app.unit.service;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.mapper.fileMapper.UploadedFileReadMapper;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UploadedFileService;
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
import java.util.Collections;
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

    @InjectMocks
    private UploadedFileService fileService;

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
        UUID expectedFileUUID = UUID.randomUUID();
        UUID expectedUserUUID = UUID.randomUUID();

        User expectedUser = User.builder()
                .userId(expectedUserUUID)
                .username("TestName")
                .password("hashed_pwd")
                .createdAt(LocalDate.now())
                .files(Collections.emptyList())
                .build();

        UploadedFile uploadedFile = UploadedFile.builder()
                .user(expectedUser)
                .id(expectedFileUUID)
                .filename("TestFileName")
                .uploadTime(Timestamp.valueOf(LocalDateTime.now()))
                .contentType(ContentType.TXT)
                .build();

        UploadedFileReadDto expectedFileDto = new UploadedFileReadDto(
                new UserReadDto(
                        expectedUser.getUserId(),
                        expectedUser.getUsername(),
                        expectedUser.getEmail(),
                        expectedUser.getCreatedAt()
                ),
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
        assertEquals(actualResult.user().userId(), expectedFileDto.user().userId());
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
                new UserReadDto(user.getUserId(), user.getUsername(), user.getEmail(), user.getCreatedAt()),
                file.getId(), file.getFilename(), file.getUploadTime(), file.getContentType()
        );

        Page<UploadedFile> page = new PageImpl<>(List.of(file), pageable, 1);

        when(uploadedFileRepository.findAllByUser_UserId(userId, pageable)).thenReturn(page);
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

        when(uploadedFileRepository.findByIdAndUser_UserId(fileId, userId)).thenReturn(Optional.of(file));
        when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(true);

        try {
            fileService.removeFile(fileId, rawPassword,userId);
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }

        verify(uploadedFileRepository).deleteById(fileId);
    }

    @Test
    void removeFile_shouldNotDeleteIfFileNotFound() {
        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String rawPassword = "1234";

        when(uploadedFileRepository.findByIdAndUser_UserId(fileId, userId)).thenReturn(Optional.empty());

        when(passwordEncoder.matches(rawPassword, any(String.class))).thenReturn(true);

        try {
            fileService.removeFile(fileId, rawPassword,userId);
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        verify(uploadedFileRepository, never()).deleteById(any());
    }
}