package by.egrius.app.dto.fileDTO;

import java.util.List;

public record UserFilesStatsDto(
        long totalFiles,
        List<UploadedFileReadDto> recentFiles
) {}