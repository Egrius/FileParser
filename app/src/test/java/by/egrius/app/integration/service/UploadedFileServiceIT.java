package by.egrius.app.integration.service;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.*;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UploadedFileService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

        user = User.builder()
                .email("emailForFun_2@gmail.com")
                .username("TestUserToUploadFile_2")
                .password(passwordEncoder.encode("1234"))
                .createdAt(LocalDate.now())
                .build();
        userRepository.save(user);
        userId = user.getUserId();
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteById(userId);
    }

    @Test
    void uploadFile_shouldPersistUploadedFileAndContent() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "hello world\nsecond line".getBytes());

        UploadedFileReadDto dto = uploadedFileService.uploadFile(file, userId);

        assertNotNull(dto);
        assertEquals("file.txt", dto.filename());

        UploadedFile persisted = uploadedFileRepository.findById(dto.id()).orElseThrow();
        assertEquals(2, persisted.getFileContent().getLineCount());
        assertEquals(4, persisted.getFileContent().getWordCount());
    }

    @Test
    void uploadEmptyFile_shouldThrowIllegalArgumentException() {
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

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
        MultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "hello world\nsecond line".getBytes());

        UploadedFileReadDto dto = uploadedFileService.uploadFile(file, userId);

        assertNotNull(dto);
        assertEquals("file.txt", dto.filename());

        UploadedFileReadDto foundDto = uploadedFileService.showUploadedFileById(userId, dto.id());

        assertNotNull(foundDto);
        assertEquals(foundDto.id(), dto.id());


    }

    @Test
    void showAllUploadedFilesByUserId_shouldReturnPage() {
        MultipartFile file1 = new MockMultipartFile("file1", "file1.txt", "text/plain", "hello world_1\nsecond line".getBytes());
        UploadedFileReadDto dto1 = uploadedFileService.uploadFile(file1, userId);

        assertNotNull(dto1);
        assertEquals("file1.txt", dto1.filename());

        MultipartFile file2 = new MockMultipartFile("file2", "file2.txt", "text/plain", "hello world_2\nsecond line".getBytes());
        UploadedFileReadDto dto2 = uploadedFileService.uploadFile(file2, userId);

        assertNotNull(dto2);
        assertEquals("file2.txt", dto2.filename());

        Pageable pageable = PageRequest.of(0,2);
        Page<UploadedFileReadDto> foundPage = uploadedFileService.showAllUploadedFilesByUserId(userId, pageable);

        List<UploadedFileReadDto> content = foundPage.getContent();

        assertEquals(2, content.size());
        List<String> filenames = content.stream().map(UploadedFileReadDto::filename).toList();
        assertTrue(filenames.containsAll(List.of("file1.txt", "file2.txt")));

    }

    @Test
    void removeFile_shouldDeleteFileIfExists() {
        MultipartFile fileToDelete = new MockMultipartFile("file1", "file1.txt", "text/plain", "hello world_1\nsecond line".getBytes());
        UploadedFileReadDto dtoToDelete = uploadedFileService.uploadFile(fileToDelete, userId);

        try {
            uploadedFileService.removeFileById(userId, "1234", dtoToDelete.id());
        } catch (AccessDeniedException e) {
            System.out.println("Пароль не подходит");
        }
    }

    @Test
    void removeFileById_shouldNotDeleteFileIfWrongPassword() {
        MultipartFile fileToDelete = new MockMultipartFile("file1", "file1.txt", "text/plain", "hello world_1\nsecond line".getBytes());
        UploadedFileReadDto dtoToDelete = uploadedFileService.uploadFile(fileToDelete, userId);
        String wrongPassword = "wrong_pass";
        assertThrows(AccessDeniedException.class,
                () -> uploadedFileService.removeFileById(userId, wrongPassword, dtoToDelete.id())
        );
        UploadedFileReadDto foundFileAfterDeletionFail =
                uploadedFileService.showUploadedFileByFilename(dtoToDelete.filename(), userId);
        assertNotNull(foundFileAfterDeletionFail);
    }


    @Test
    void removeFileByFilename_shouldNotDeleteIfWrongPassword() {
        MultipartFile fileToDelete = new MockMultipartFile("file", "file.txt", "text/plain", "hello world".getBytes());
        UploadedFileReadDto dtoToDelete = uploadedFileService.uploadFile(fileToDelete, userId);

        assertThrows(AccessDeniedException.class,
                () -> uploadedFileService.removeFileByFilename(userId, "wrong_pass", dtoToDelete.filename())
        );

        UploadedFileReadDto found = uploadedFileService.showUploadedFileByFilename(dtoToDelete.filename(), userId);
        assertNotNull(found);
    }

    @Test
    void removeFile_shouldThrowIfFileNotFound() {
        UUID randomFileId = UUID.randomUUID();
        assertThrows(EntityNotFoundException.class,
                () -> uploadedFileService.removeFileById(userId, "1234", randomFileId)
        );
    }

    @Test
    @Transactional
    void anotherUserCantRemoveOthersFile_shouldThrowEntityNotFound() {
        MultipartFile fileToDelete = new MockMultipartFile("file", "file.txt", "text/plain", "hello world".getBytes());
        UploadedFileReadDto dtoToDelete = uploadedFileService.uploadFile(fileToDelete, userId);

        User anotherUser = User.builder()
                .email("emailForFun_ANOTHER@gmail.com")
                .username("TestUserAnother")
                .password(passwordEncoder.encode("1234"))
                .createdAt(LocalDate.now())
                .build();

        userRepository.save(anotherUser);
        userRepository.flush();

        UUID anotherUserId = userRepository.findByUsername("TestUserAnother").get().getUserId();

        assertNotNull(anotherUserId);

        MultipartFile anotherUserFile= new MockMultipartFile("file", "file.txt", "text/plain", "hello world".getBytes());
        UploadedFileReadDto dtoAnotherUserFile = uploadedFileService.uploadFile(anotherUserFile, anotherUserId);

        assertThrows(EntityNotFoundException.class,
                () -> uploadedFileService.removeFileById(userId, "1234", dtoAnotherUserFile.id())
        );
    }

}