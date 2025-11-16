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
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileEventLogService {

    private final FileEventLogRepository fileEventLogRepository;
    private final UploadedFileRepository uploadedFileRepository;

    private void createEventLog(FileEventType eventType, UUID fileId) {
        if (eventType == FileEventType.DELETED) {

            FileEventLog event = FileEventLog.builder()
                    .fileEventType(eventType)
                    .uploadedFile(null)
                    .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                    .build();


            fileEventLogRepository.save(event);
            return;
        }

        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("Не найден файл для лога"));

        FileEventLog event = FileEventLog.builder()
                .fileEventType(eventType)
                .uploadedFile(file)
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        fileEventLogRepository.save(event);
    }

    public void log(FileEvent event) {
        if (event.getFileEventType() == null || event.getFileId() == null) {
            System.out.println("Некорректное событие: отсутствует тип или ID файла");
            return;
        }
        createEventLog(event.getFileEventType(), event.getFileId());
    }

}
