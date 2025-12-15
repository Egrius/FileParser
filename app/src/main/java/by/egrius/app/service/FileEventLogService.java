package by.egrius.app.service;

import by.egrius.app.dto.fileDTO.FileReadDto;
import by.egrius.app.entity.FileEventLog;
import by.egrius.app.entity.UploadedFile;
import by.egrius.app.entity.enums.FileEventType;
import by.egrius.app.event.FileEvent;
import by.egrius.app.repository.FileEventLogRepository;
import by.egrius.app.repository.UploadedFileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileEventLogService {

    private final FileEventLogRepository fileEventLogRepository;
    private final UploadedFileRepository uploadedFileRepository;

    private void createEventLog(FileEventType eventType, UUID fileId) {
        FileEventLog eventLog = FileEventLog.builder()
                .fileEventType(eventType)
                .fileId(fileId)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        if (eventType != FileEventType.DELETED) {
            UploadedFile file = uploadedFileRepository.findById(fileId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Файл %s не найден для события %s", fileId, eventType)));
        }

        fileEventLogRepository.save(eventLog);
    }

    public void log(FileEvent event) {
        if (event == null || event.getFileEventType() == null || event.getFileId() == null) {
            log.warn("Попытка залогировать некорректное событие: {}", event);
            return;
        }
        try {
            createEventLog(event.getFileEventType(), event.getFileId());
            log.debug("Событие {} залогировано для файла {}",
                    event.getFileEventType(), event.getFileId());
        } catch (Exception e) {
            log.error("Ошибка при логировании события {} для файла {}",
                    event.getFileEventType(), event.getFileId(), e);
            throw new IllegalStateException("Не удалось залогировать событие", e);
        }
    }

}
