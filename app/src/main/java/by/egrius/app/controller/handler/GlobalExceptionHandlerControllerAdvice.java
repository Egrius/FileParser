package by.egrius.app.controller.handler;

import by.egrius.app.dto.ExceptionDto;
import by.egrius.app.dto.ValidationErrorDto;
import by.egrius.app.dto.ViolationDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandlerControllerAdvice {
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorDto onConstraintValidationException(ConstraintViolationException e) {
        System.out.println("Вызван обработчик onConstraintValidationException\n");
        ValidationErrorDto error = new ValidationErrorDto();
        for(ConstraintViolation v : e.getConstraintViolations()) {
            error.getViolations().add(
                    new ViolationDto(v.getPropertyPath().toString(), v.getMessage())
            );
        }
        return error;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorDto handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        System.out.println("Вызван обработчик handleMethodArgumentNotValid\n");
        ValidationErrorDto error = new ValidationErrorDto();
        e.getBindingResult().getFieldErrors().forEach(
                fieldError -> error.getViolations().add(
                        new ViolationDto(fieldError.getField(), fieldError.getDefaultMessage()
                        )
                )
        );
        return error;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ExceptionDto handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        return new ExceptionDto(
                e.getMessage(),
                "BAD_REQUEST",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.BAD_REQUEST.value()
        );
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ExceptionDto handleUnexpected(Exception e, HttpServletRequest request) {
        return new ExceptionDto(
                e.getMessage(),
                "INTERNAL_ERROR",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    ExceptionDto onAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        return new ExceptionDto(
                e.getMessage(),
                "ACCESS_DENIED_ERROR",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.FORBIDDEN.value()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                         HttpServletRequest request) {

        String message;
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            message = String.format("Параметр '%s' должен быть валидным UUID", ex.getName());
        } else {
            message = String.format("Некорректный формат параметра '%s'", ex.getName());
        }

        return new ExceptionDto(
                message,
                "BAD_REQUEST",
                request.getRequestURI(),
                LocalDate.now(),
                HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                             HttpServletRequest request) {
        return new ExceptionDto(
                String.format("Отсутствует обязательный параметр: '%s'", ex.getParameterName()),
                        "BAD_REQUEST",
                        request.getRequestURI(),
                        LocalDate.now(),
                        HttpStatus.BAD_REQUEST.value()
        );
    }
}
