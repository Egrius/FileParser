package by.egrius.app.controller;

import by.egrius.app.dto.fileDTO.FileReadDto;
import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.service.UploadedFileService;
import by.egrius.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private UploadedFileService uploadedFileService;

    @Autowired
    private UserService userService;

    // Реализовать логику передачи айди из секюрити контекста
    @PostMapping("/upload")
    public ResponseEntity<UploadedFileReadDto> uploadFile(@RequestParam("file") MultipartFile fileToUpload) {

        UUID userId = userService.getCurrentUser().getUserId();

        UploadedFileReadDto uploaded = uploadedFileService.uploadFile(fileToUpload, userId);
        return ResponseEntity.ok(uploaded);
    }

    @GetMapping("/show-files")
    public ResponseEntity<Page<UploadedFileReadDto>> showUploadedFile(@RequestParam int page,
                                                                      @RequestParam int pageSize) {
        UUID userId = userService.getCurrentUser().getUserId();

        Page<UploadedFileReadDto> allFilesPage = uploadedFileService.showAllUploadedFilesByUserId(userId,
                PageRequest.of(page, pageSize));

        return ResponseEntity.ok(allFilesPage);
    }

    @GetMapping("/by-filename")
    public ResponseEntity<UploadedFileReadDto> getUploadedFileByFilename(@RequestParam String filename) {
        UUID userId = userService.getCurrentUser().getUserId();

        UploadedFileReadDto uploadedFileReadDto = uploadedFileService.showUploadedFileByFilename(filename, userId);
        return ResponseEntity.ok(uploadedFileReadDto);
    }

    @GetMapping("/by-fileId")
    public ResponseEntity<UploadedFileReadDto> getUploadedFileByFileId(@RequestParam UUID fileId) {
        UUID userId = userService.getCurrentUser().getUserId();

        UploadedFileReadDto uploadedFileReadDto = uploadedFileService.showUploadedFileById(userId, fileId);
        return ResponseEntity.ok(uploadedFileReadDto);
    }
}
