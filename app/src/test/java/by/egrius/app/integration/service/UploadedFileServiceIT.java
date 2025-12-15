package by.egrius.app.integration.service;

import by.egrius.app.TestUtils;
import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.*;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UploadedFileService;
import by.egrius.app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("service-test")
@Import({
        ServiceTestConfig.class,
        UploadedFileService.class,
        UserService.class
})
class UploadedFileServiceIT {

    @Autowired
    private UploadedFileService uploadedFileService;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID userId;
    private User user;

    @BeforeEach
    void setup() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        user = TestUtils.createTestUser(userRepository, passwordEncoder, suffix);
        userId = user.getUserId();
        TestUtils.setupSecurityContext(user);
    }

    @AfterEach
    void tearDown() {
        TestUtils.clearSecurityContext();
    }

    @Test
    void uploadFile_shouldPersistUploadedFileAndContent() throws Exception {

        MultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "hello world\nsecond line".getBytes()
        );

        UploadedFileReadDto dto = uploadedFileService.uploadFile(file, userId);

        assertNotNull(dto);
        assertEquals("file.txt", dto.filename());
        assertNotNull(dto.id());

        var persisted = uploadedFileRepository.findById(dto.id());
        assertTrue(persisted.isPresent());
    }

    @Test
    void uploadEmptyFile_shouldThrowIllegalArgumentException() {

        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );


        assertThrows(IllegalArgumentException.class,
                () -> uploadedFileService.uploadFile(emptyFile, userId)
        );
    }

    @Test
    void uploadFileWithoutName_shouldUseUnnamedTxt() {

        MultipartFile fileWithoutName = new MockMultipartFile(
                "file",
                null,
                "text/plain",
                "content".getBytes()
        );

        UploadedFileReadDto dto = uploadedFileService.uploadFile(fileWithoutName, userId);

        assertNotNull(dto);
        assertEquals("unnamed.txt", dto.filename());
    }

    @Test
    void showUploadedFileByItsId() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "hello world\nsecond line".getBytes()
        );

        UploadedFileReadDto uploadedDto = uploadedFileService.uploadFile(file, userId);
        assertNotNull(uploadedDto);

        UploadedFileReadDto foundDto = uploadedFileService.showUploadedFileById(userId, uploadedDto.id());

        assertNotNull(foundDto);
        assertEquals(uploadedDto.id(), foundDto.id());
        assertEquals("file.txt", foundDto.filename());
    }

    @Test
    void showAllUploadedFilesByUserId_shouldReturnPage() {

        MultipartFile file1 = new MockMultipartFile(
                "file1",
                "file1.txt",
                "text/plain",
                "content 1".getBytes()
        );
        uploadedFileService.uploadFile(file1, userId);

        MultipartFile file2 = new MockMultipartFile(
                "file2",
                "file2.txt",
                "text/plain",
                "content 2".getBytes()
        );
        uploadedFileService.uploadFile(file2, userId);

        Pageable pageable = PageRequest.of(0, 10);

        Page<UploadedFileReadDto> foundPage = uploadedFileService.showAllUploadedFilesByUserId(userId, pageable);

        assertEquals(2, foundPage.getTotalElements());
        List<String> filenames = foundPage.getContent().stream()
                .map(UploadedFileReadDto::filename)
                .toList();
        assertTrue(filenames.contains("file1.txt"));
        assertTrue(filenames.contains("file2.txt"));
    }

    @Test
    void removeFile_shouldDeleteFileIfExists() throws AccessDeniedException {

        MultipartFile fileToDelete = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "content".getBytes()
        );

        UploadedFileReadDto uploadedDto = uploadedFileService.uploadFile(fileToDelete, userId);
        UUID fileId = uploadedDto.id();

        assertNotNull(uploadedFileService.showUploadedFileById(userId, fileId));

        uploadedFileService.removeFileById(userId, "1234", fileId);

        assertThrows(EntityNotFoundException.class,
                () -> uploadedFileService.showUploadedFileById(userId, fileId)
        );
    }

    @Test
    void removeFileById_shouldNotDeleteFileIfWrongPassword() {
        // Arrange
        MultipartFile fileToDelete = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "content".getBytes()
        );

        UploadedFileReadDto uploadedDto = uploadedFileService.uploadFile(fileToDelete, userId);
        UUID fileId = uploadedDto.id();

        assertThrows(AccessDeniedException.class,
                () -> uploadedFileService.removeFileById(userId, "wrong_password", fileId)
        );

        UploadedFileReadDto foundFile = uploadedFileService.showUploadedFileById(userId, fileId);
        assertNotNull(foundFile);
    }

    @Test
    void removeFileByFilename_shouldNotDeleteIfWrongPassword() {

        String filename = "file.txt";
        MultipartFile file = new MockMultipartFile(
                "file",
                filename,
                "text/plain",
                "content".getBytes()
        );

        uploadedFileService.uploadFile(file, userId);

        assertThrows(AccessDeniedException.class,
                () -> uploadedFileService.removeFileByFilename(userId, "wrong_password", filename)
        );

        UploadedFileReadDto foundFile = uploadedFileService.showUploadedFileByFilename(filename, userId);
        assertNotNull(foundFile);
    }

    @Test
    void removeFile_shouldThrowIfFileNotFound() {

        UUID nonExistentFileId = UUID.randomUUID();

        assertThrows(EntityNotFoundException.class,
                () -> uploadedFileService.removeFileById(userId, "1234", nonExistentFileId)
        );
    }

    @Test
    void anotherUserCantRemoveOthersFile_shouldThrowEntityNotFound() {

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        User anotherUser = User.builder()
                .email("another_" + uniqueSuffix + "@gmail.com")
                .username("AnotherUser_" + uniqueSuffix)
                .password(passwordEncoder.encode("1234"))
                .createdAt(LocalDate.now())
                .build();

        anotherUser = userRepository.save(anotherUser);
        UUID anotherUserId = anotherUser.getUserId();

        MultipartFile anotherUserFile = new MockMultipartFile(
                "file",
                "another_file.txt",
                "text/plain",
                "content".getBytes()
        );

        UploadedFileReadDto anotherUserDto = uploadedFileService.uploadFile(anotherUserFile, anotherUserId);

        assertThrows(EntityNotFoundException.class,
                () -> uploadedFileService.removeFileById(userId, "1234", anotherUserDto.id())
        );
    }

    @Test
    void showUploadedFileById_withWrongUser_shouldThrowEntityNotFound() {

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        User anotherUser = User.builder()
                .email("another2_" + uniqueSuffix + "@gmail.com")
                .username("AnotherUser2_" + uniqueSuffix)
                .password(passwordEncoder.encode("1234"))
                .createdAt(LocalDate.now())
                .build();

        anotherUser = userRepository.save(anotherUser);
        UUID anotherUserId = anotherUser.getUserId();

        MultipartFile file = new MockMultipartFile(
                "file",
                "myfile.txt",
                "text/plain",
                "content".getBytes()
        );

        UploadedFileReadDto uploadedDto = uploadedFileService.uploadFile(file, userId);

        assertThrows(EntityNotFoundException.class,
                () -> uploadedFileService.showUploadedFileById(anotherUserId, uploadedDto.id())
        );
    }

    @Test
    void uploadMultipleFiles_shouldAllBeRetrievable() {

        MultipartFile file1 = new MockMultipartFile("file1", "file1.txt", "text/plain", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file2", "file2.txt", "text/plain", "content2".getBytes());
        MultipartFile file3 = new MockMultipartFile("file3", "file3.txt", "text/plain", "content3".getBytes());

        UploadedFileReadDto dto1 = uploadedFileService.uploadFile(file1, userId);
        UploadedFileReadDto dto2 = uploadedFileService.uploadFile(file2, userId);
        UploadedFileReadDto dto3 = uploadedFileService.uploadFile(file3, userId);

        assertNotNull(dto1.id());
        assertNotNull(dto2.id());
        assertNotNull(dto3.id());

        Page<UploadedFileReadDto> page = uploadedFileService.showAllUploadedFilesByUserId(
                userId,
                PageRequest.of(0, 10)
        );

        assertEquals(3, page.getTotalElements());
    }
}