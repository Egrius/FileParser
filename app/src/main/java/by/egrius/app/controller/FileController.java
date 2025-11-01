package by.egrius.app.controller;

import by.egrius.app.dto.fileDTO.FileReadDto;
import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.service.UploadedFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private UploadedFileService uploadedFileService;

    @PostMapping("/upload")
    public ResponseEntity<UploadedFileReadDto> uploadFile(@RequestParam("user-id") UUID userId,
                                                          @RequestParam("file") MultipartFile fileToUpload) {

        UploadedFileReadDto uploaded = uploadedFileService.uploadFile(fileToUpload, userId);
        return ResponseEntity.ok(uploaded);
    }

    // Реализовать логику передачи айди из секюрити контекста
    @GetMapping("show-file/{id}")
    public ResponseEntity<UploadedFileReadDto> showUploadedFile(@PathVariable UUID fileId) {
        //UploadedFileReadDto uploadedFileReadDto = uploadedFileService.showUploadedFileById()
        return null;
    }

}
