package by.egrius.app.dto.fileDTO;

import by.egrius.app.dto.userDTO.UserReadDto;
import by.egrius.app.entity.enums.ContentType;

import java.sql.Timestamp;
import java.util.UUID;

public record FileReadDto (
        UserReadDto user,
        RegexMatchReadDto regexMatch,
        FileContentReadDto fileContent,
        UUID id,
        String filename,
        Timestamp uploadTime,
        ContentType contentType
) {}
