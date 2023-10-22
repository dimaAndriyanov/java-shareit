package ru.practicum.shareit.exception;

public class CanNotUpdateBookingStatusException extends RuntimeException {
    public CanNotUpdateBookingStatusException(String message) {
        super(message);
    }
}