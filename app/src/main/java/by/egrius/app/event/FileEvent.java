package by.egrius.app.event;


import by.egrius.app.entity.enums.FileEventType;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@ToString
public class FileEvent extends ApplicationEvent {
    private final UUID fileId;
    private final FileEventType fileEventType;
    private final Timestamp timestampEvent;


    public FileEvent(Object source, UUID fileId, FileEventType fileEventType, Timestamp timestampEvent) {
        super(source);
        this.fileId = fileId;
        this.fileEventType = fileEventType;
        this.timestampEvent = timestampEvent;
    }
}
