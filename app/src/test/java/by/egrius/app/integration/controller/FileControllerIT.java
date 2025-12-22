package by.egrius.app.integration.controller;

import by.egrius.app.dto.request.FileAnalysisRequestDto;
import by.egrius.app.dto.request.FileDeleteRequestDto;
import by.egrius.app.dto.request.StopWordsUpdateDto;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.PatternType;
import by.egrius.app.repository.UserRepository;
import by.egrius.app.service.FileAnalysisService;
import by.egrius.app.service.RegexMatchService;
import by.egrius.app.service.UploadedFileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class FileControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UploadedFileService uploadedFileService;

    @Autowired
    private FileAnalysisService fileAnalysisService;

    @Autowired
    private RegexMatchService regexMatchService;

    @Autowired
    private UserRepository userRepository;

    @Nested
    class uploadTests {

        private MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "testFile.txt",
                "text/plain",
                "Test text".getBytes());

        private UUID userId;

        @BeforeEach
        void setup() {

            userId = UUID.randomUUID();

            User user = User.builder()
                    .username("TestUser")
                    .email("testUserEmail@gmail.com")
                    .password("1234")
                    .build();

            userId = userRepository.save(user).getUserId();
        }

        @AfterEach
        void cleanup() {
            userRepository.deleteById(userId);
        }

        @Test
        void uploadFile_shouldReturn200IfCorrectData() throws Exception {
            mockMvc.perform(multipart("/file/upload")
                    .file(multipartFile)
                    .with(httpBasic("TestUser", "1234"))
                    .contentType("multipart/form-data")
            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.filename").value("testFile.txt"))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.contentType").exists())
                    .andExpect(jsonPath("$.uploadTime").exists());

            assertNotNull(uploadedFileService.showUploadedFileByFilename("testFile.txt", userId));
        }

        @Test
        void uploadFile_shouldReturn400WhenFileSizeExceedsLimit() throws Exception {
            byte[] largeContent = new byte[11 * 1024 * 1024];
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file",
                    "large.txt",
                    "text/plain",
                    largeContent
            );

            mockMvc.perform(multipart("/file/upload")
                            .file(largeFile)
                            .with(httpBasic("TestUser", "1234"))
                            .contentType("multipart/form-data")
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void uploadFile_shouldReturn400WhenFileIsEmpty() throws Exception {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.txt",
                    "text/plain",
                    new byte[0]
            );

            mockMvc.perform(multipart("/file/upload")
                            .file(emptyFile)
                            .with(httpBasic("TestUser", "1234"))
                            .contentType("multipart/form-data")
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void uploadFile_shouldReturn400WhenFileContainsOnlyWhitespace() throws Exception {
            MockMultipartFile whitespaceFile = new MockMultipartFile(
                    "file",
                    "whitespace.txt",
                    "text/plain",
                    "   \n\t   ".getBytes()
            );

            mockMvc.perform(multipart("/file/upload")
                            .file(whitespaceFile)
                            .with(httpBasic("TestUser", "1234"))
                            .contentType("multipart/form-data")
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void uploadFile_shouldReturn400WhenFilenameAlreadyExists() throws Exception {
            mockMvc.perform(multipart("/file/upload")
                            .file(multipartFile)
                            .with(httpBasic("TestUser", "1234"))
                            .contentType("multipart/form-data")
                    )
                    .andExpect(status().isOk());

            mockMvc.perform(multipart("/file/upload")
                            .file(multipartFile)
                            .with(httpBasic("TestUser", "1234"))
                            .contentType("multipart/form-data")
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void uploadFile_shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(multipart("/file/upload")
                            .file(multipartFile)
                            .contentType("multipart/form-data")
                    )
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void uploadFile_shouldReturn401WhenInvalidCredentials() throws Exception {
            mockMvc.perform(multipart("/file/upload")
                            .file(multipartFile)
                            .with(httpBasic("TestUser", "wrongpassword"))
                            .contentType("multipart/form-data")
                    )
                    .andExpect(status().isUnauthorized());
        }

    }

    @Nested
    class showUploadedTests {

        private MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "testFile.txt",
                "text/plain",
                "Test text".getBytes());

        private MockMultipartFile secondFile = new MockMultipartFile(
                "file",
                "secondFile.txt",
                "text/plain",
                "Second file content".getBytes());

        private UUID userId;
        private UUID fileId;
        private UUID secondFileId;

        @BeforeEach
        void setup() throws Exception {
            userId = UUID.randomUUID();

            User user = User.builder()
                    .username("TestUser")
                    .email("testUserEmail@gmail.com")
                    .password("1234")
                    .build();

            userId = userRepository.save(user).getUserId();
            fileId = uploadedFileService.uploadFile(multipartFile, userId).id();
            secondFileId = uploadedFileService.uploadFile(secondFile, userId).id();
        }

        @AfterEach
        void cleanup() {
            userRepository.deleteById(userId);
        }

        @Test
        void showUploadedFiles_shouldReturn200IfCorrectData() throws Exception {
            int page = 0;
            int pageSize = 2;

            mockMvc.perform(get("/file/show-files")
                            .param("page", String.valueOf(page))
                            .param("pageSize", String.valueOf(pageSize))
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].filename").exists())
                    .andExpect(jsonPath("$.content[1].filename").exists())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        void showUploadedFiles_shouldReturnEmptyPageWhenNoFiles() throws Exception {
            // Создаем нового пользователя без файлов
            User user2 = User.builder()
                    .username("User2")
                    .email("user2@test.com")
                    .password("1234")
                    .build();
            UUID user2Id = userRepository.save(user2).getUserId();

            mockMvc.perform(get("/file/show-files")
                            .param("page", "0")
                            .param("pageSize", "10")
                            .with(httpBasic("User2", "1234"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));

            userRepository.deleteById(user2Id);
        }

        @Test
        void showUploadedFiles_shouldReturnPagedResults() throws Exception {
            // Создаем больше файлов для тестирования пагинации
            for (int i = 0; i < 5; i++) {
                MockMultipartFile extraFile = new MockMultipartFile(
                        "file",
                        "file" + i + ".txt",
                        "text/plain",
                        ("Content " + i).getBytes()
                );
                uploadedFileService.uploadFile(extraFile, userId);
            }

            // Первая страница - 2 элемента
            mockMvc.perform(get("/file/show-files")
                            .param("page", "0")
                            .param("pageSize", "2")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(7));

            mockMvc.perform(get("/file/show-files")
                            .param("page", "1")
                            .param("pageSize", "2")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        void showUploadedFiles_shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/file/show-files")
                            .param("page", "0")
                            .param("pageSize", "10")
                    )
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void showUploadedFiles_shouldReturn400WhenInvalidPagination() throws Exception {
            // Отрицательная страница
            mockMvc.perform(get("/file/show-files")
                            .param("page", "-1")
                            .param("pageSize", "10")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isBadRequest());

            // Нулевой размер страницы
            mockMvc.perform(get("/file/show-files")
                            .param("page", "0")
                            .param("pageSize", "0")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isBadRequest());

            // Отрицательный размер страницы
            mockMvc.perform(get("/file/show-files")
                            .param("page", "0")
                            .param("pageSize", "-10")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void showUploadedFiles_shouldOnlyShowCurrentUserFiles() throws Exception {
            // Создаем второго пользователя с файлом
            User user2 = User.builder()
                    .username("User2")
                    .email("user2@test.com")
                    .password("1234")
                    .build();
            UUID user2Id = userRepository.save(user2).getUserId();

            MockMultipartFile user2File = new MockMultipartFile(
                    "file",
                    "user2file.txt",
                    "text/plain",
                    "User2 content".getBytes()
            );
            uploadedFileService.uploadFile(user2File, user2Id);

            // User1 должен видеть только свои файлы
            mockMvc.perform(get("/file/show-files")
                            .param("page", "0")
                            .param("pageSize", "10")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2)); // только 2 файла User1

            // User2 должен видеть только свой файл
            mockMvc.perform(get("/file/show-files")
                            .param("page", "0")
                            .param("pageSize", "10")
                            .with(httpBasic("User2", "1234"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1)); // только 1 файл User2

            userRepository.deleteById(user2Id);
        }

        @Test
        void getUploadedFileByFilename_shouldReturn200IfCorrectData() throws Exception {
            mockMvc.perform(get("/file/by-filename")
                            .param("filename", "testFile.txt")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(fileId.toString()))
                    .andExpect(jsonPath("$.filename").value("testFile.txt"))
                    .andExpect(jsonPath("$.contentType").value("TXT"))
                    .andExpect(jsonPath("$.uploadTime").exists());
        }

        @Test
        void getUploadedFileByFilename_shouldReturn404WhenFileNotFound() throws Exception {
            mockMvc.perform(get("/file/by-filename")
                            .param("filename", "nonexistent.txt")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void getUploadedFileByFilename_shouldReturn404WhenFileBelongsToAnotherUser() throws Exception {
            // Создаем второго пользователя с файлом
            User user2 = User.builder()
                    .username("User2")
                    .email("user2@test.com")
                    .password("1234")
                    .build();
            UUID user2Id = userRepository.save(user2).getUserId();

            MockMultipartFile user2File = new MockMultipartFile(
                    "file",
                    "user2file.txt",
                    "text/plain",
                    "User2 content".getBytes()
            );
            uploadedFileService.uploadFile(user2File, user2Id);

            // User1 пытается получить файл User2
            mockMvc.perform(get("/file/by-filename")
                            .param("filename", "user2file.txt")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isNotFound());

            userRepository.deleteById(user2Id);
        }

        @Test
        void getUploadedFileByFileId_shouldReturn200IfCorrectData() throws Exception {
            mockMvc.perform(get("/file/by-fileId")
                            .param("fileId", fileId.toString())
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(fileId.toString()))
                    .andExpect(jsonPath("$.filename").value("testFile.txt"));
        }

        @Test
        void getUploadedFileByFileId_shouldReturn404WhenFileNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/file/by-fileId")
                            .param("fileId", nonExistentId.toString())
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void getUploadedFileByFileId_shouldReturn404WhenFileBelongsToAnotherUser() throws Exception {
            // Создаем второго пользователя с файлом
            User user2 = User.builder()
                    .username("User2")
                    .email("user2@test.com")
                    .password("1234")
                    .build();
            UUID user2Id = userRepository.save(user2).getUserId();

            MockMultipartFile user2File = new MockMultipartFile(
                    "file",
                    "user2file.txt",
                    "text/plain",
                    "User2 content".getBytes()
            );
            UUID user2FileId = uploadedFileService.uploadFile(user2File, user2Id).id();

            // User1 пытается получить файл User2
            mockMvc.perform(get("/file/by-fileId")
                            .param("fileId", user2FileId.toString())
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isNotFound());

            userRepository.deleteById(user2Id);
        }

        @Test
        void getUploadedFileByFileId_shouldReturn400WhenInvalidFileIdFormat() throws Exception {
            mockMvc.perform(get("/file/by-fileId")
                            .param("fileId", "invalid-uuid-format")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void getUploadedFileByFileId_shouldReturn400WhenMissingFileId() throws Exception {
            mockMvc.perform(get("/file/by-fileId")
                            .with(httpBasic("TestUser", "1234"))
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class deleteTests {

        private MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "testFile.txt",
                "text/plain",
                "Test text".getBytes());

        private UUID userId;
        private UUID fileId;

        @BeforeEach
        void setup() throws Exception {
            userId = UUID.randomUUID();

            User user = User.builder()
                    .username("TestUser")
                    .email("testUserEmail@gmail.com")
                    .password("1234")
                    .build();

            userId = userRepository.save(user).getUserId();
            fileId = uploadedFileService.uploadFile(multipartFile, userId).id();
        }

        @AfterEach
        void cleanup() {
            userRepository.deleteById(userId);
        }

        @Test
        void deleteFileById_shouldDeleteIfCorrectData() throws Exception {

            FileDeleteRequestDto deleteRequestDto = new FileDeleteRequestDto(fileId, "1234");

            mockMvc.perform(delete("/file/delete/by-id")
                    .with(httpBasic("TestUser", "1234"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deleteRequestDto)))
                    .andExpect(status().isOk());

            assertThrows(EntityNotFoundException.class,
                    () -> uploadedFileService.showUploadedFileById(userId, fileId));
        }


        @Test
        void deleteFileById_shouldReturnForbiddenWhenPasswordIsIncorrect() throws Exception {

            FileDeleteRequestDto deleteRequestDto = new FileDeleteRequestDto(fileId, "wrongPass");

            mockMvc.perform(delete("/file/delete/by-id")
                            .with(httpBasic("TestUser", "1234"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deleteRequestDto)))
                    .andDo(result -> {

                        System.out.println("Status: " + result.getResponse().getStatus());
                        System.out.println("Body: " + result.getResponse().getContentAsString());
                        if (result.getResolvedException() != null) {
                            System.out.println("Exception: " + result.getResolvedException().getClass());
                            result.getResolvedException().printStackTrace();
                        }
                    })
                    .andExpect(status().isForbidden());
        }
    }
    // ============ ANALYSIS TESTS ============
    @Nested
    class AnalysisTests {
        private UUID userId;
        private UUID fileId;

        @BeforeEach
        void setup() {

            userId = UUID.randomUUID();

            User user = User.builder()
                    .username("TestUser")
                    .email("testUserEmail@gmail.com")
                    .password("1234")
                    .build();

            userId = userRepository.save(user).getUserId();

            String text = """
                    Hello world! This is a test file for analysis.
                    It contains multiple words and sentences.
                    Testing analysis functionality with various words.
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "analysis.txt", "text/plain", text.getBytes());
            fileId = uploadedFileService.uploadFile(file, userId).id();
        }

        @AfterEach
        void cleanup() {
            userRepository.deleteById(userId);
        }


        @Test
        void analyzeFile_shouldReturn201WhenAnalysisCreated() throws Exception {
            FileAnalysisRequestDto request = new FileAnalysisRequestDto(5, false);

            mockMvc.perform(post("/file/{fileId}/analyze", fileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.topWords").exists());
        }

        @Test
        void getFileAnalysis_shouldReturn404WhenNoAnalysis() throws Exception {
            mockMvc.perform(get("/file/{fileId}/analysis", fileId)
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getFileAnalysis_shouldReturn200WhenAnalysisExists() throws Exception {
            // Сначала создаем анализ
            fileAnalysisService.createAnalysis(fileId, 5, false);

            mockMvc.perform(get("/file/{fileId}/analysis", fileId)
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.topWords").exists());
        }
    }

    // ============ REGEX TESTS ============
    @Nested
    class RegexTests {
        private UUID userId;
        private UUID fileId;

        @BeforeEach
        void setup(){
            User user = User.builder()
                    .username("TestUser")
                    .email("test@test.com")
                    .password("1234")
                    .build();
            userId = userRepository.save(user).getUserId();

            // Файл с различными паттернами
            String text = """
                    Email: test@example.com
                    Phone: +375 (29) 123-45-67
                    IP: 192.168.1.1
                    Date: 25.12.2023
                    Another email: user@domain.org
                    """;
            MockMultipartFile file = new MockMultipartFile(
                    "file", "regex-test.txt", "text/plain", text.getBytes());
            fileId = uploadedFileService.uploadFile(file, userId).id();
        }

        @AfterEach
        void cleanup() {
            userRepository.deleteById(userId);
        }

        @Test
        void findPatterns_shouldReturn201WhenPatternsFound() throws Exception {
            Set<PatternType> patterns = Set.of(PatternType.EMAIL, PatternType.PHONE);

            mockMvc.perform(post("/file/{fileId}/regex", fileId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patterns))
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.matchCount").exists());
        }

        @Test
        void getRegexMatches_shouldReturn404WhenNoMatches() throws Exception {
            mockMvc.perform(get("/file/{fileId}/regex", fileId)
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getPatternMatchesByType_shouldReturnMatches() throws Exception {
            // Сначала создаем поиск
            Set<PatternType> patterns = Set.of(PatternType.EMAIL);
            regexMatchService.createRegexMatch(fileId, patterns);

            mockMvc.perform(get("/file/{fileId}/regex/{patternType}", fileId, "EMAIL")
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ============ STATS TESTS ============
    @Nested
    class StatsTests {
        private UUID userId;
        private UUID fileId1;
        private UUID fileId2;

        @BeforeEach
        void setup() throws Exception {
            User user = User.builder()
                    .username("TestUser")
                    .email("test@test.com")
                    .password("1234")
                    .build();
            userId = userRepository.save(user).getUserId();

            // Загружаем несколько файлов
            MockMultipartFile file1 = new MockMultipartFile(
                    "file", "stats1.txt", "text/plain", "Content 1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile(
                    "file", "stats2.txt", "text/plain", "Content 2".getBytes());

            fileId1 = uploadedFileService.uploadFile(file1, userId).id();
            fileId2 = uploadedFileService.uploadFile(file2, userId).id();

            // Создаем анализ для одного файла
            fileAnalysisService.createAnalysis(fileId1, 3, false);
        }

        @AfterEach
        void cleanup() {
            userRepository.deleteById(userId);
        }

        @Test
        void getFileStats_shouldReturnStats() throws Exception {
            mockMvc.perform(get("/file/{fileId}/stats", fileId1)
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.file.id").value(fileId1.toString()))
                    .andExpect(jsonPath("$.hasAnalysis").value(true));
        }

        @Test
        void getUserFilesStats_shouldReturnUserStats() throws Exception {
            mockMvc.perform(get("/file/user/stats")
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalFiles").value(2))
                    .andExpect(jsonPath("$.recentFiles").isArray());
        }
    }

    // ============ SEARCH TESTS ============
    @Nested
    class SearchTests {
        private UUID userId;

        @BeforeEach
        void setup() throws Exception {
            User user = User.builder()
                    .username("TestUser")
                    .email("test@test.com")
                    .password("1234")
                    .build();
            userId = userRepository.save(user).getUserId();

            // Загружаем разные файлы
            uploadedFileService.uploadFile(
                    new MockMultipartFile("file", "document.txt", "text/plain", "Doc".getBytes()),
                    userId);
            uploadedFileService.uploadFile(
                    new MockMultipartFile("file", "report.txt", "text/plain", "Report".getBytes()),
                    userId);
            uploadedFileService.uploadFile(
                    new MockMultipartFile("file", "data.json", "application/json", "{}".getBytes()),
                    userId);
        }

        @AfterEach
        void cleanup() {
            userRepository.deleteById(userId);
        }

        @Test
        void searchFiles_shouldReturnMatchingFiles() throws Exception {
            mockMvc.perform(get("/file/search")
                            .param("keyword", "doc")
                            .param("page", "0")
                            .param("pageSize", "10")
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].filename").value("document.txt"));
        }

        @Test
        void filterByContentType_shouldReturnFilteredFiles() throws Exception {
            mockMvc.perform(get("/file/filter/by-type")
                            .param("contentType", MediaType.APPLICATION_JSON_VALUE)
                            .param("page", "0")
                            .param("pageSize", "10")
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].filename").value("data.json"));
        }
    }

    // ============ STOP WORDS TESTS ============
    @Nested
    class StopWordsTests {

        private UUID userId;

        @BeforeEach
        void setup() throws Exception {
            User user = User.builder()
                    .username("TestUser")
                    .email("test@test.com")
                    .password("1234")
                    .build();
            userId = userRepository.save(user).getUserId();


        }

        @AfterEach
        void cleanup() {
            userRepository.deleteById(userId);
        }


        @Test
        void updateStopWords_shouldReturn200() throws Exception {
            StopWordsUpdateDto request = new StopWordsUpdateDto("the,and,or");

            mockMvc.perform(put("/file/analysis/stopwords")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isOk());
        }

        @Test
        void updateStopWords_shouldReturn400WhenInvalid() throws Exception {
            // Пустые стоп-слова
            StopWordsUpdateDto request = new StopWordsUpdateDto("");

            mockMvc.perform(put("/file/analysis/stopwords")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(httpBasic("TestUser", "1234")))
                    .andExpect(status().isBadRequest());
        }
    }
}