package by.egrius.app.controller;

import by.egrius.app.dto.ValidationErrorDto;
import by.egrius.app.dto.ViolationDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {
    @ExceptionHandler(exception = {
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorDto onConstraintValidationException(ConstraintViolationException e) {
        ValidationErrorDto error = new ValidationErrorDto();
        for(ConstraintViolation v : e.getConstraintViolations()) {
            error.getViolations().add(
                    new ViolationDto(v.getPropertyPath().toString(), v.getMessage())
            );
        }
        return error;
    }

    @ExceptionHandler(exception = {
            EntityNotFoundException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    Map<String, String> onEntityNotFoundException(EntityNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Map<String, String> handleSecurity(SecurityException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorDto handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        ValidationErrorDto error = new ValidationErrorDto();
        e.getBindingResult().getFieldErrors().forEach(
                fieldError -> error.getViolations().add(
                        new ViolationDto(fieldError.getField(), fieldError.getDefaultMessage()
                        )
                )
        );

        return error;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleUnexpected(Exception ex) {
        return Map.of("error", "Внутренняя ошибка сервера");
    }


}
