package by.egrius.app.dto.fileDTO;

import by.egrius.app.entity.enums.FileEventType;

import java.sql.Timestamp;

public record FIleEventReadDto (
        FileEventType fileEventType,
        Timestamp timestamp
) {}
