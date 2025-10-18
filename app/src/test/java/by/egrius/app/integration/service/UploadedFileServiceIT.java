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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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

    @BeforeAll
    void setup() {
        User user = User.builder()
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
}