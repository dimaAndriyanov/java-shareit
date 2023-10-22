package ru.practicum.shareit.exception;

public class EmailIsAlreadyInUseException extends RuntimeException {
    public EmailIsAlreadyInUseException(String message) {
        super(message);
    }
}