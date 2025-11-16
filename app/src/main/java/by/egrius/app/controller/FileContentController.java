package by.egrius.app.controller;

import by.egrius.app.dto.fileDTO.FileContentReadDto;
import by.egrius.app.security.UserPrincipal;
import by.egrius.app.service.UploadedFileService;
import by.egrius.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController("/file-content")
public class FileContentController {

    @Autowired
    private UserService userService;

    @Autowired
    private UploadedFileService uploadedFileService;


    @GetMapping("show/{fileId}")
    public ResponseEntity<FileContentReadDto> getFileContentById(@PathVariable("fileId") UUID fileId,
                                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID userId = userPrincipal.getId();
        FileContentReadDto fileContentReadDto = uploadedFileService.getFileContent(userId, fileId);
        return ResponseEntity.ok(fileContentReadDto);
    }

}