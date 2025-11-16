package by.egrius.app.controller.handler;

import by.egrius.app.controller.UserController;
import by.egrius.app.dto.ExceptionDto;
import by.egrius.app.dto.ValidationErrorDto;
import by.egrius.app.dto.ViolationDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ControllerAdvice(assignableTypes = {UserController.class})
public class ExceptionHandlerUserControllerAdvice {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ExceptionDto onEntityAlreadyExistsException(DataIntegrityViolationException e, HttpServletRequest request) {
        return new ExceptionDto(
                "Пользователь с таким email или username уже существует",
                "DATA_INTEGRITY_ERROR",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ExceptionDto onEntityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        return new ExceptionDto(
                e.getMessage(),
                "ENTITY_NOT_FOUND_ERROR",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.NOT_FOUND.value()
        );
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ExceptionDto handleSecurity(SecurityException e, HttpServletRequest request) {
        return new ExceptionDto(
                e.getMessage(),
                "SECURITY_ERROR",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.FORBIDDEN.value()
        );
    }




}
