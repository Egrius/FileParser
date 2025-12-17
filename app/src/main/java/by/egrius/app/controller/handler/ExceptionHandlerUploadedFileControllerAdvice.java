package by.egrius.app.controller.handler;

import by.egrius.app.controller.FileController;
import by.egrius.app.dto.ExceptionDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;

@ControllerAdvice(assignableTypes = {FileController.class})
public class ExceptionHandlerUploadedFileControllerAdvice {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ExceptionDto onEntityAlreadyExistsException(DataIntegrityViolationException e, HttpServletRequest request) {
        return new ExceptionDto(
                "Файл с таким именем уже существует",
                "DATA_INTEGRITY_ERROR",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    ExceptionDto onEntityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        return new ExceptionDto(
                e.getMessage(),
                "ENTITY_NOT_FOUND_ERROR",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.NOT_FOUND.value()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ExceptionDto onEntityNotFoundException(IllegalArgumentException e, HttpServletRequest request) {
        return new ExceptionDto(
                e.getMessage(),
                "ILLEGAL_ARGUMENT_ERROR",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.NOT_FOUND.value()
        );
    }


}
