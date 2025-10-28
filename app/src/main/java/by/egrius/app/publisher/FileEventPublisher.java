package by.egrius.app.publisher;

import by.egrius.app.entity.enums.FileEventType;
import by.egrius.app.event.FileEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(FileEventType type, UUID fileId) {
        FileEvent event = new FileEvent(this, fileId, type, Timestamp.valueOf(LocalDateTime.now()));
        eventPublisher.publishEvent(event);
    }

    public void publishUpload(UUID fileId) {
        publish(FileEventType.UPLOAD, fileId);
    }

    public void publishParsed(UUID fileId) {
        publish(FileEventType.PARSE_END, fileId);
    }

    public void publishDeleted(UUID fileId) {
        publish(FileEventType.DELETED, fileId);
    }
}
