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
import ru.practicum.shareit.exception.*;

@RestControllerAdvice("ru.practicum.shareit")
@Slf4j
public class ErrorHandler {
    @ExceptionHandler({
            NotAvailableItemException.class,
            CanNotUpdateBookingStatusException.class,
            PostingCommentWithoutCompletedBookingException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestError(Throwable exception) {
        log.warn("Bad request received.\n{}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler ({EmailIsAlreadyInUseException.class, BookingDatesIntersectWithAlreadyExistingBookingException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlePuttingConflictingDataError(Throwable exception) {
        log.warn("Request on putting object conflicting with already existing objects has been received\n{}",
                exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundError(ObjectNotFoundException exception) {
        log.warn("Requested object not found.\n{}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleDataAccessException(DataAccessException exception) {
        log.warn("Request on changing item not from owner has been received\n{}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleBadRequestFromGateway(Throwable exception) {
        log.error("Error on receiving request from gateway occurred.\n{}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(Throwable exception) {
        log.error("Error occurred.\n{}", exception.getMessage());
        return new ErrorResponse(exception.getMessage());
    }
}