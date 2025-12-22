package by.egrius.app.controller;

import by.egrius.app.dto.fileDTO.*;
import by.egrius.app.dto.request.FileAnalysisRequestDto;
import by.egrius.app.dto.request.FileDeleteRequestDto;
import by.egrius.app.dto.request.StopWordsUpdateDto;
import by.egrius.app.dto.response.PageResponse;
import by.egrius.app.entity.enums.PatternType;
import by.egrius.app.security.UserPrincipal;
import by.egrius.app.service.FileAnalysisService;
import by.egrius.app.service.RegexMatchService;
import by.egrius.app.service.UploadedFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final UploadedFileService uploadedFileService;
    private final FileAnalysisService fileAnalysisService;
    private final RegexMatchService regexMatchService;

    @PostMapping("/upload")
    public ResponseEntity<UploadedFileReadDto> uploadFile(@RequestParam("file") MultipartFile fileToUpload,
                                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UUID userId = userPrincipal.getId();
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

    @DeleteMapping("/delete/by-id")
    public ResponseEntity<Void> deleteFileById(@Valid @RequestBody FileDeleteRequestDto requestDto,
                                               @AuthenticationPrincipal UserPrincipal userPrincipal)
            throws AccessDeniedException {

        UUID userId = userPrincipal.getId();
        uploadedFileService.removeFileById(userId, requestDto.rawPassword(), requestDto.fileId());
        return ResponseEntity.ok().build();
    }

    // ============ АНАЛИЗ ТЕКСТА ============

    @PostMapping("/{fileId}/analyze")
    public ResponseEntity<FileAnalysisReadDto> analyzeFile(
            @PathVariable UUID fileId,
            @Valid @RequestBody FileAnalysisRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Проверка прав доступа
        uploadedFileService.showUploadedFileById(userPrincipal.getId(), fileId);

        FileAnalysisReadDto analysis = fileAnalysisService.createAnalysis(
                fileId, request.topN(), request.excludeStopWords());

        return ResponseEntity.status(HttpStatus.CREATED).body(analysis);
    }

    @GetMapping("/{fileId}/analysis")
    public ResponseEntity<FileAnalysisReadDto> getFileAnalysis(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        uploadedFileService.showUploadedFileById(userPrincipal.getId(), fileId);

        return fileAnalysisService.getAnalysisByFileId(fileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/analysis/stopwords")
    public ResponseEntity<Void> updateStopWords(
            @Valid @RequestBody StopWordsUpdateDto updateDto) {

        fileAnalysisService.setStopWordsRaw(updateDto.stopWords());
        return ResponseEntity.ok().build();
    }

    // ============ REGEX ПОИСК ============

    @PostMapping("/{fileId}/regex")
    public ResponseEntity<RegexMatchReadDto> findPatterns(
            @PathVariable UUID fileId,
            @RequestBody Set<PatternType> patternTypes,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        uploadedFileService.showUploadedFileById(userPrincipal.getId(), fileId);

        RegexMatchReadDto matches = regexMatchService.createRegexMatch(fileId, patternTypes);
        return ResponseEntity.status(HttpStatus.CREATED).body(matches);
    }

    @GetMapping("/{fileId}/regex")
    public ResponseEntity<RegexMatchReadDto> getRegexMatches(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        uploadedFileService.showUploadedFileById(userPrincipal.getId(), fileId);

        return regexMatchService.getRegexMatchByFileId(fileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{fileId}/regex/{patternType}")
    public ResponseEntity<List<PatternMatchDto>> getPatternMatchesByType(
            @PathVariable UUID fileId,
            @PathVariable PatternType patternType,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        uploadedFileService.showUploadedFileById(userPrincipal.getId(), fileId);

        List<PatternMatchDto> matches = regexMatchService.getPatternMatchesByType(fileId, patternType)
                .stream()
                .map(pm -> new PatternMatchDto(pm.getPatternType(), pm.getMatch()))
                .toList();

        return ResponseEntity.ok(matches);
    }

    @DeleteMapping("/{fileId}/regex")
    public ResponseEntity<Void> deleteRegexAnalysis(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        uploadedFileService.showUploadedFileById(userPrincipal.getId(), fileId);
        regexMatchService.deleteRegexMatch(fileId);
        return ResponseEntity.noContent().build();
    }

    // ============ СТАТИСТИКА И ИНФОРМАЦИЯ ============

    @GetMapping("/{fileId}/stats")
    public ResponseEntity<FileStatsDto> getFileStats(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UploadedFileReadDto file = uploadedFileService.showUploadedFileById(userPrincipal.getId(), fileId);

        FileAnalysisReadDto analysis = fileAnalysisService.getAnalysisByFileId(fileId).orElse(null);
        RegexMatchReadDto regex = regexMatchService.getRegexMatchByFileId(fileId).orElse(null);

        FileStatsDto stats = new FileStatsDto(
                file,
                analysis,
                regex,
                analysis != null,
                regex != null
        );

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user/stats")
    public ResponseEntity<UserFilesStatsDto> getUserFilesStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UUID userId = userPrincipal.getId();

        // Получаем базовую статистику
        long totalFiles = uploadedFileService.countFilesByUserId(userId);
        List<UploadedFileReadDto> recentFiles = uploadedFileService.getRecentFiles(userId, 5);

        UserFilesStatsDto stats = new UserFilesStatsDto(
                totalFiles,
                recentFiles
        );

        return ResponseEntity.ok(stats);
    }

    // ============ ПОИСК И ФИЛЬТРАЦИЯ ============

    @GetMapping("/search")
    public ResponseEntity<PageResponse<UploadedFileReadDto>> searchFiles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UUID userId = userPrincipal.getId();
        Page<UploadedFileReadDto> results = uploadedFileService.searchFiles(
                userId, keyword, PageRequest.of(page, pageSize));

        PageResponse<UploadedFileReadDto> response = new PageResponse<>(
                results.getContent(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter/by-type")
    public ResponseEntity<PageResponse<UploadedFileReadDto>> filterByContentType(
            @RequestParam String contentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UUID userId = userPrincipal.getId();
        Page<UploadedFileReadDto> results = uploadedFileService.filterByContentType(
                userId, contentType, PageRequest.of(page, pageSize));

        PageResponse<UploadedFileReadDto> response = new PageResponse<>(
                results.getContent(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

}