package by.egrius.app.integration.service;

import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.User;
import by.egrius.app.repository.UploadedFileRepository;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.UploadedFileService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @BeforeAll
    void setup() {
         user = User.builder()
                .userId(UUID.randomUUID())
                .email("emailForFun_2@gmail.com")
                .username("TestUserToUploadFile_2")
                .password(passwordEncoder.encode("1234"))
                .createdAt(LocalDate.now())
                .build();
        userRepository.save(user);
        userId = user.getUserId();
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
            uploadedFileService.removeFile(userId, "1234", dtoToDelete.id());
        } catch (AccessDeniedException e) {
            System.out.println("Пароль не подходит");
        }
    }
}