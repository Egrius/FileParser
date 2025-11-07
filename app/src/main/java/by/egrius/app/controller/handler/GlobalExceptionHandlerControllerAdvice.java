package by.egrius.app.controller.handler;

import by.egrius.app.dto.ExceptionDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ControllerAdvice
public class GlobalExceptionHandlerControllerAdvice {
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
}
