package ru.practicum.shareit.exception;

public class EmptyObjectException extends RuntimeException {
    public EmptyObjectException(String message) {
        super(message);
    }
}