package ru.practicum.shareit.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.error.ErrorResponse;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.*;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice("ru.practicum.shareit")
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<FieldViolation> handleBodyValidationError(FieldValidationException exception) {
        log.warn("Bad request received. Request body failed validation\n{}", exception.getFieldViolations());
        return exception.getFieldViolations();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<FieldViolation> handleVariablesValidationError(ConstraintViolationException exception) {
        List<FieldViolation> fieldViolations = exception.getConstraintViolations().stream()
                .map(violation -> new FieldViolation(violation.getPropertyPath().toString(), violation.getMessage()))
                .collect(Collectors.toList());
        log.warn("Bad request received. Request variables failed validation\n{}", fieldViolations);
        return fieldViolations;
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            UnsupportedStateException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestError(Throwable exception) {
        log.warn("Bad request received.\n{}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEmptyObjectError(EmptyObjectException exception) {
        log.warn("Bad request received. Received body has no filled fields\n{}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(Throwable exception) {
        log.error("Error occurred.\n{}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }
}