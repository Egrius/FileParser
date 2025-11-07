package by.egrius.app.dto;

import java.time.LocalDate;

public record ExceptionDto (
        String message,
        String code,
        String path,
        LocalDate timestamp,
        int status
){}
