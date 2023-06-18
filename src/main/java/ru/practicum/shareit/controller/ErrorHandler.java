package ru.practicum.shareit.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.error.ErrorResponse;
import ru.practicum.shareit.error.FieldViolation;
import ru.practicum.shareit.exception.EmailIsAlreadyInUseException;
import ru.practicum.shareit.exception.EmptyObjectException;
import ru.practicum.shareit.exception.FieldValidationException;
import ru.practicum.shareit.exception.ObjectNotFoundException;

import java.util.List;

@RestControllerAdvice("ru.practicum.shareit")
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<FieldViolation> handleBodyValidationError(FieldValidationException exception) {
        log.warn("Bad request received. Request body failed validation");
        return exception.getFieldViolations();
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestError(RuntimeException exception) {
        log.warn("Bad request received. {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEmptyObjectError(EmptyObjectException exception) {
        log.warn("Bad request received. Received body has no filled fields");
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlePuttingConflictingDataError(EmailIsAlreadyInUseException exception) {
        log.warn("Request on putting object conflicting with already existing objects has been received");
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundError(ObjectNotFoundException exception) {
        log.warn("Requested object not found. {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(Throwable exception) {
        log.error("Error occurred. {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }
}