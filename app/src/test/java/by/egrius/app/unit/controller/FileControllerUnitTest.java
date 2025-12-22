package by.egrius.app.unit.controller;

import by.egrius.app.controller.FileController;
import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.response.PageResponse;
import by.egrius.app.entity.User;
import by.egrius.app.entity.enums.ContentType;
import by.egrius.app.security.UserPrincipal;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerUnitTest {

    @Mock
    private UploadedFileService uploadedFileService;

    @InjectMocks
    private FileController fileController;

    @Test
    void uploadFile_shouldUploadFileIfUserIsCorrect(){

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "testFile.txt",
                "text/plain",
                "Test text".getBytes()
        );

        UUID fileId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Timestamp uploadedTime = Timestamp.valueOf(LocalDateTime.now());

        UploadedFileReadDto expected = new UploadedFileReadDto(
                fileId, "testFile.txt", uploadedTime, ContentType.TXT
        );

        User user = new User(
                userId,
                "FileTestUser",
                "test@gmail.com",
                "1234",
                LocalDate.now(),
                Collections.emptyList()
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(uploadedFileService.uploadFile(any(), eq(userId)))
                .thenReturn(expected);

        ResponseEntity<UploadedFileReadDto> response =
                fileController.uploadFile(mockMultipartFile, userPrincipal);


        assertEquals(200, HttpStatus.OK.value());
        assertNotNull(response.getBody());
        assertEquals(fileId, response.getBody().id());
        assertEquals("testFile.txt", response.getBody().filename());

        verify(uploadedFileService).uploadFile(mockMultipartFile, userId);
    }

    @Test
    void showAllUploadedFilesByUserId_shouldShowFilesIfCorrect() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int pageSize = 2;

        User user = new User(
                userId,
                "FileTestUser",
                "test@gmail.com",
                "1234",
                LocalDate.now(),
                Collections.emptyList()
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);


        Timestamp uploadedTime = Timestamp.valueOf(LocalDateTime.now());

        UploadedFileReadDto file1Dto = new UploadedFileReadDto(
                UUID.randomUUID(), "testFile1.txt", uploadedTime, ContentType.TXT
        );

        UploadedFileReadDto file2Dto = new UploadedFileReadDto(
                UUID.randomUUID(), "testFile2.txt", uploadedTime, ContentType.TXT
        );

        List<UploadedFileReadDto> content = List.of(file1Dto, file2Dto);
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<UploadedFileReadDto> mockPage = new PageImpl<>(
                content,
                pageable,
                content.size()
        );

        when(uploadedFileService.showAllUploadedFilesByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(mockPage);

        ResponseEntity<PageResponse<UploadedFileReadDto>> response =
                fileController.showUploadedFiles(page, pageSize, userPrincipal);

        assertEquals(200, HttpStatus.OK.value());
        assertNotNull(response.getBody());

        PageResponse<UploadedFileReadDto> pageResponse = response.getBody();

        assertEquals(2, pageResponse.content().size());
        assertEquals(page, pageResponse.page());
        assertEquals(pageSize, pageResponse.size());
        assertEquals(2L, pageResponse.totalElements());

        assertEquals("testFile1.txt", pageResponse.content().get(0).filename());
        assertEquals("testFile2.txt", pageResponse.content().get(1).filename());

        verify(uploadedFileService).showAllUploadedFilesByUserId(userId, PageRequest.of(page, pageSize));
    }

    @Test
    void showByFileId_shouldReturnCorrectFile() {
        UUID userId = UUID.randomUUID();

        User user = new User(
                userId,
                "FileTestUser",
                "test@gmail.com",
                "1234",
                LocalDate.now(),
                Collections.emptyList()
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        UUID fileId = UUID.randomUUID();
        Timestamp uploadedTime = Timestamp.valueOf(LocalDateTime.now());
        UploadedFileReadDto expectedFileReadDto = new UploadedFileReadDto(fileId, "test.txt", uploadedTime, ContentType.TXT);

        when(uploadedFileService.showUploadedFileById(eq(userId), eq(fileId))).thenReturn(expectedFileReadDto);

        ResponseEntity<UploadedFileReadDto> result = fileController.getUploadedFileByFileId(fileId, userPrincipal);

        UploadedFileReadDto resultBody = result.getBody();

        assertNotNull(resultBody);
        assertEquals(resultBody.id(), expectedFileReadDto.id());
        assertEquals(resultBody.filename(), expectedFileReadDto.filename());
        assertEquals(resultBody.uploadTime(), expectedFileReadDto.uploadTime());
    }
}