package by.egrius.app.controller;

import by.egrius.app.dto.fileDTO.FileContentReadDto;
import by.egrius.app.dto.fileDTO.FileReadDto;
import by.egrius.app.dto.fileDTO.UploadedFileReadDto;
import by.egrius.app.dto.request.FileDeleteRequestDto;
import by.egrius.app.dto.response.PageResponse;
import by.egrius.app.entity.User;
import by.egrius.app.security.UserPrincipal;
import by.egrius.app.service.UploadedFileService;
import by.egrius.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private UploadedFileService uploadedFileService;

    @Autowired
    private UserService userService;

    // Переделать юнит тесты для контроллера и сервиса тоже!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    @PostMapping("/upload")
    public ResponseEntity<UploadedFileReadDto> uploadFile(@RequestParam("file") MultipartFile fileToUpload,
                                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UUID userId = userPrincipal.getUser().getUserId();
        UploadedFileReadDto uploaded = uploadedFileService.uploadFile(fileToUpload, userId);
        return ResponseEntity.ok(uploaded);
    }

    @GetMapping("/show-files")
    public ResponseEntity<PageResponse<UploadedFileReadDto>> showUploadedFiles(@RequestParam int page,
                                                                              @RequestParam int pageSize,
                                                                              @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID userId = userPrincipal.getId();

        Page<UploadedFileReadDto> allFilesPage = uploadedFileService.showAllUploadedFilesByUserId(userId,
                PageRequest.of(page, pageSize));

        PageResponse<UploadedFileReadDto> response = new PageResponse<>(
                allFilesPage.getContent(),
                allFilesPage.getNumber(),
                allFilesPage.getSize(),
                allFilesPage.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-filename")
    public ResponseEntity<UploadedFileReadDto> getUploadedFileByFilename(@RequestParam String filename,
                                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID userId = userPrincipal.getId();
        UploadedFileReadDto uploadedFileReadDto = uploadedFileService.showUploadedFileByFilename(filename, userId);
        return ResponseEntity.ok(uploadedFileReadDto);
    }

    @GetMapping("/by-fileId")
    public ResponseEntity<UploadedFileReadDto> getUploadedFileByFileId(@RequestParam UUID fileId,
                                                                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID userId = userPrincipal.getId();
        UploadedFileReadDto uploadedFileReadDto = uploadedFileService.showUploadedFileById(userId, fileId);
        return ResponseEntity.ok(uploadedFileReadDto);
    }


//Затестить
    @DeleteMapping("/delete/by-id")
    public ResponseEntity<Void> deleteFileById(@Valid @RequestBody FileDeleteRequestDto requestDto,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal)
            throws AccessDeniedException {

        UUID userId = userPrincipal.getId();
        uploadedFileService.removeFile(userId, requestDto.rawPassword(), requestDto.fileId());
        return ResponseEntity.ok().build();
    }


}
